package com.github.mustachejava.jruby;

import com.github.mustachejava.TemplateFunction;
import com.github.mustachejava.reflect.Guard;
import com.github.mustachejava.reflect.ReflectionObjectHandler;
import com.github.mustachejava.util.Wrapper;
import org.jruby.RubyBoolean;
import org.jruby.RubyHash;
import org.jruby.RubyObject;
import org.jruby.RubyProc;
import org.jruby.RubySymbol;
import org.jruby.embed.ScriptingContainer;

import java.lang.reflect.Method;
import java.util.List;

public class JRubyObjectHandler extends ReflectionObjectHandler {

  private static final Method CALL_METHOD;
  private static ScriptingContainer sc = new ScriptingContainer();

  static {
    try {
      CALL_METHOD = RubyHash.class.getMethod("callMethod", String.class);
    } catch (NoSuchMethodException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public Object coerce(Object object) {
    if (object instanceof RubyBoolean) {
      RubyBoolean rb = (RubyBoolean) object;
      return rb.toJava(Boolean.class);
    }
    if (object instanceof RubyProc) {
      final RubyProc proc = (RubyProc) object;
      return new TemplateFunction() {
        @Override
        public String apply(String s) {
          TemplateFunction fun = sc.getInstance(proc, TemplateFunction.class);
          return fun.apply(s);
        }
      };
    }
    return object;
  }

  @Override
  protected Wrapper findWrapper(final int scopeIndex, final Wrapper[] wrappers, final List<Guard> guards, final Object scope, final String name) {
    if (scope instanceof RubyHash) {
      RubyHash hash = (RubyHash) scope;
      final RubySymbol rs = RubySymbol.newSymbol(hash.getRuntime(), name);
      if (hash.containsKey(rs)) {
        guards.add(new Guard() {
          @Override
          public boolean apply(Object[] input) {
            return ((RubyHash) input[scopeIndex]).containsKey(rs);
          }
        });
        return createWrapper(scopeIndex, wrappers, guards, MAP_METHOD, new Object[]{rs});
      } else {
        guards.add(new Guard() {
          @Override
          public boolean apply(Object[] input) {
            return !((RubyHash) input[scopeIndex]).containsKey(rs);
          }
        });
      }
    }
    if (scope instanceof RubyObject) {
      RubyObject ro = (RubyObject) scope;
      if (ro.respondsTo(name)) {
        guards.add(new Guard() {
          @Override
          public boolean apply(Object[] objects) {
            RubyObject scope = (RubyObject) objects[scopeIndex];
            return scope.respondsTo(name);
          }
        });
        return createWrapper(scopeIndex, wrappers, guards, CALL_METHOD, new Object[]{name});
      } else {
        guards.add(new Guard() {
          @Override
          public boolean apply(Object[] objects) {
            RubyObject scope = (RubyObject) objects[scopeIndex];
            return !scope.respondsTo(name);
          }
        });
      }
    }
    return super.findWrapper(scopeIndex, wrappers, guards, scope, name);
  }
}
