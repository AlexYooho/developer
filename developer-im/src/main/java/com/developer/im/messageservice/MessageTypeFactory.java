package com.developer.im.messageservice;

import com.developer.framework.utils.SpringContext;
import com.developer.im.enums.MessageMainType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public abstract class MessageTypeFactory {

    private static List<AbstractMessageTypeService> messageMainTypeList = new ArrayList<>();

    public void initAllMessageTypeService() {
        if(messageMainTypeList.isEmpty()){
            List<Class<?>> subClasses = getSubClasses(AbstractMessageTypeService.class);
            for (Class<?> subclass : subClasses){
                AbstractMessageTypeService item = (AbstractMessageTypeService) SpringContext.getApplicationContext().getBean(subclass);
                messageMainTypeList.add(item);
            }
        }
    }

    public AbstractMessageTypeService getMessageProcessor(MessageMainType messageMainType){
        initAllMessageTypeService();
        List<AbstractMessageTypeService> collect = messageMainTypeList.stream().filter(x -> x.messageMainType() == messageMainType).collect(Collectors.toList());
        if(!collect.isEmpty()) {
            return collect.get(0);
        }
        return null;
    }

    public static List<Class<?>> getSubClasses(Class<?> abstractClass) {
        List<Class<?>> subClasses = new ArrayList<>();

        // 获取所有类
        Package[] packages = Package.getPackages();
        for (Package pkg : packages) {
            String pkgName = pkg.getName();
            List<Class<?>> classes = getClasses(pkgName);

            // 遍历每个类，找到继承自抽象类的子类
            for (Class<?> clazz : classes) {
                if (abstractClass.isAssignableFrom(clazz) && !abstractClass.equals(clazz)) {
                    if(!subClasses.contains(clazz)) {
                        subClasses.add(clazz);
                    }
                }
            }
        }

        return subClasses;
    }

    public static List<Class<?>> getClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');

        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(packagePath);
            List<File> directories = new ArrayList<>();

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                directories.add(new File(resource.getFile()));
            }

            for (File directory : directories) {
                classes.addAll(findClasses(directory, packageName));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return classes;
    }

    private static Collection<? extends Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }

        return classes;
    }
}
