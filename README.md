# UniversalProxy

[![Download](https://api.bintray.com/packages/wulugongshe/maven/universal-proxy/images/download.svg) ](https://bintray.com/wulugongshe/maven/universal-proxy/_latestVersion)

# 一个android或java项目的编译时注解动态代理库，功能强大，可以生成接口、抽象类、普通类等的代理

### gradle 
    
       compile 'com.lazymc:universal-proxy:1.0.0'
    
### 使用方法 
        
        @ProxyInject
        public class ClassTest {
            public boolean test(String test) {
                return false;
            }
        }

        final ClassTest classTestHost = new ClassTest();
               try {
                   ClassTest classTest = ProxyFactory.createProxy(ClassTest.class, new InvocationHandler() {
                       @Override
                       public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                           return method.invoke(classTestHost, args);
                       }
                   });
                   classTest.test("");
               } catch (Exception e) {
                   e.printStackTrace();
               }