package com.sky.integration;

import com.sky.SkyApplication;
import com.sky.support.DatabaseTestSupport;
import com.sky.utils.AliOssUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes=SkyApplication.class,webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Stage1FlowIT extends DatabaseTestSupport {
    @Autowired TestRestTemplate http;
    @LocalServerPort int port;
    @MockitoBean AliOssUtil storage;

    private ResponseEntity<String> call(HttpMethod method,String path,Object body,String auth) {
        HttpHeaders headers=new HttpHeaders();
        if(auth!=null) headers.set("Authorization",auth);
        headers.setContentType(body instanceof LinkedMultiValueMap ? MediaType.MULTIPART_FORM_DATA : MediaType.APPLICATION_JSON);
        return http.exchange("http://localhost:"+port+path,method,new HttpEntity<>(body,headers),String.class);
    }
    private String token(String username) throws Exception {
        var response=call(HttpMethod.POST,"/admin/employee/login",Map.of("username",username,"password",PASSWORD),null);
        assertEquals(200,response.getStatusCode().value());
        return "Bearer "+json.readTree(response.getBody()).path("data").path("token").asText();
    }
    private LinkedMultiValueMap<String,Object> imageForm() throws Exception {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(2,2,BufferedImage.TYPE_INT_RGB),"png",bytes);
        ByteArrayResource resource=new ByteArrayResource(bytes.toByteArray()) { @Override public String getFilename() { return "test.png"; } };
        HttpHeaders headers=new HttpHeaders();headers.setContentType(MediaType.IMAGE_PNG);
        LinkedMultiValueMap<String,Object> form=new LinkedMultiValueMap<>();form.add("file",new HttpEntity<>(resource,headers));return form;
    }

    @Test void fullFlowAcrossRealHttpAndMysqlWithMockedCloudBoundary() throws Exception {
        when(storage.upload(anyString(),any(byte[].class),eq("image/png"))).thenAnswer(call->"https://images.example.com/"+call.getArgument(0));
        String admin=token("admin");
        var created=call(HttpMethod.POST,"/admin/employee",Map.of("username","flow_user","name","Flow user","password",PASSWORD,"phone","13800000001"),admin);
        assertEquals(200,created.getStatusCode().value());long userId=json.readTree(created.getBody()).path("data").asLong();
        String worker=token("flow_user");
        var category=call(HttpMethod.POST,"/admin/category",Map.of("name","Flow category","type",1,"sort",0),worker);
        assertEquals(200,category.getStatusCode().value());long categoryId=json.readTree(category.getBody()).path("data").asLong();
        assertEquals(userId,jdbc.queryForObject("SELECT create_user FROM category WHERE id=?",Long.class,categoryId));
        assertEquals(200,call(HttpMethod.POST,"/admin/category/status/1?id="+categoryId,null,worker).getStatusCode().value());
        var list=call(HttpMethod.GET,"/admin/category/list?type=1",null,worker);
        assertEquals(1,json.readTree(list.getBody()).path("data").size());
        var upload=call(HttpMethod.POST,"/admin/common/upload",imageForm(),worker);
        assertEquals(200,upload.getStatusCode().value());assertTrue(json.readTree(upload.getBody()).path("data").asText().startsWith("https://images.example.com/"));
        assertEquals(200,call(HttpMethod.POST,"/admin/employee/status/0?id="+userId,null,admin).getStatusCode().value());
        assertEquals(403,call(HttpMethod.GET,"/admin/category/page",null,worker).getStatusCode().value());
    }
    @Test void documentationAndOpenApiAreAvailableAnonymously() throws Exception {
        assertEquals(200,http.getForEntity("http://localhost:"+port+"/doc.html",String.class).getStatusCode().value());
        var diagnostic=mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/v3/api-docs")).andReturn();
        if (diagnostic.getResolvedException()!=null) {
            Throwable failure=diagnostic.getResolvedException();
            while (failure.getCause()!=null) failure=failure.getCause();
            fail("OpenAPI exception: "+failure.getClass().getName()+": "+failure.getMessage());
        }
        var document=http.getForEntity("http://localhost:"+port+"/v3/api-docs",String.class);
        assertEquals(200,document.getStatusCode().value());
        var paths=json.readTree(document.getBody()).path("paths");
        assertTrue(paths.has("/admin/common/upload"));assertTrue(paths.has("/admin/employee/page"));assertTrue(paths.has("/admin/category/page"));
        var loginSecurity=paths.path("/admin/employee/login").path("post").path("security");
        assertTrue(loginSecurity.isArray() && loginSecurity.isEmpty(),"Login documentation must override the global authentication requirement");
    }
    @Test void uploadRequiresLoginAndHandlesStorageFailureUniformly() throws Exception {
        assertEquals(401,call(HttpMethod.POST,"/admin/common/upload",imageForm(),null).getStatusCode().value());
        when(storage.upload(anyString(),any(byte[].class),anyString())).thenThrow(new com.sky.exception.BusinessException(502,"Storage unavailable"));
        var response=call(HttpMethod.POST,"/admin/common/upload",imageForm(),token("admin"));
        assertEquals(502,response.getStatusCode().value());assertEquals(0,json.readTree(response.getBody()).path("code").asInt());
    }
    @Test void unsupportedMethodHasUniform405Response() throws Exception {
        var response=call(HttpMethod.PATCH,"/admin/employee",Map.of(),token("admin"));
        assertEquals(405,response.getStatusCode().value());assertEquals(0,json.readTree(response.getBody()).path("code").asInt());
    }
    @Test void oversizedMultipartReturnsUniform400Response() throws Exception {
        ByteArrayResource resource=new ByteArrayResource(new byte[5*1024*1024+1]) { @Override public String getFilename() { return "oversized.png"; } };
        HttpHeaders headers=new HttpHeaders();headers.setContentType(MediaType.IMAGE_PNG);
        LinkedMultiValueMap<String,Object> form=new LinkedMultiValueMap<>();form.add("file",new HttpEntity<>(resource,headers));
        var response=call(HttpMethod.POST,"/admin/common/upload",form,token("admin"));
        assertEquals(400,response.getStatusCode().value());assertEquals(0,json.readTree(response.getBody()).path("code").asInt());
        verifyNoInteractions(storage);
    }
    @Test void malformedMultipartAndWrongContentTypeAreClientErrors() throws Exception {
        HttpHeaders loginHeaders=new HttpHeaders();loginHeaders.setContentType(MediaType.TEXT_PLAIN);
        var wrongType=http.exchange("http://localhost:"+port+"/admin/employee/login",HttpMethod.POST,new HttpEntity<>("invalid",loginHeaders),String.class);
        assertEquals(400,wrongType.getStatusCode().value());assertEquals(0,json.readTree(wrongType.getBody()).path("code").asInt());
        HttpHeaders uploadHeaders=new HttpHeaders();uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);uploadHeaders.set("Authorization",token("admin"));
        var broken=http.exchange("http://localhost:"+port+"/admin/common/upload",HttpMethod.POST,new HttpEntity<>("no boundary",uploadHeaders),String.class);
        assertEquals(400,broken.getStatusCode().value());assertEquals(0,json.readTree(broken.getBody()).path("code").asInt());
    }
}
