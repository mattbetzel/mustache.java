package com.github.mustachejava.simple;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.SpecTest;
import com.github.mustachejava.reflect.SimpleObjectHandler;
import org.codehaus.jackson.JsonNode;
import org.junit.Ignore;

@Ignore
public class SimpleSpecTest extends SpecTest {
  @Override
  protected DefaultMustacheFactory createMustacheFactory(JsonNode test) {
    DefaultMustacheFactory mf = super.createMustacheFactory(test);
    mf.setObjectHandler(new SimpleObjectHandler());
    return mf;
  }
}
