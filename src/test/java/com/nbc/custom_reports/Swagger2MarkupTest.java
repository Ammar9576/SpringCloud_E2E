package com.nbc.custom_reports;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.nbc.custom_reports.CustomReportsApplication;

import springfox.documentation.staticdocs.Swagger2MarkupResultHandler;
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CustomReportsApplication.class, loader = SpringBootContextLoader.class)  
public class Swagger2MarkupTest {

  
	private static final String API_URI = "/v2/api-docs";     

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private File projectDir;

    @Before
    public void setup() throws IOException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();

        /*ClassPathResource pathfileRes = new ClassPathResource("target/classes/application.yml");
        projectDir = pathfileRes.getFile().getParentFile().getParentFile().getParentFile().getParentFile();*/
    }

    @Test
    public void convertSwaggerToAsciiDoc() throws Exception {
        Swagger2MarkupResultHandler.Builder builder = Swagger2MarkupResultHandler
            .outputDirectory(outputDirForFormat("asciidoc"));
        mockMvc.perform(get(API_URI).accept(MediaType.APPLICATION_JSON))
            .andDo(builder.build())
            .andExpect(status().isOk());

    }

    private String outputDirForFormat(String format) throws IOException {
        return new File(projectDir, "target/classes/static/docs/" + format + "/generated").getAbsolutePath();
    }
}