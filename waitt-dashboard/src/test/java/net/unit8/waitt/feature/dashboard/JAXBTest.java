package net.unit8.waitt.feature.dashboard;

import org.junit.Test;

import javax.xml.bind.JAXB;
import java.io.Serializable;
import java.io.StringReader;

public class JAXBTest {
    private static class TestBean implements Serializable {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    @Test
    public void jaxbTest() {
        StringReader sr = new StringReader("<name>kawasima</name>");
        final TestBean testBean = JAXB.unmarshal(sr, TestBean.class);
        System.out.println(testBean);
    }
}
