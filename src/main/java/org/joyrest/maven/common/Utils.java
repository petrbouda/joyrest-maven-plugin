package org.joyrest.maven.common;

import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.io.*;
import java.nio.file.*;
import java.util.List;

public final class Utils {

    public static void createFolder(Path path) {
        if (!Files.exists(path))
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                throw new RuntimeException("Error occurred during creating a folder for plugin 'gendoc'", e);
            }
    }

    public static void loadClassPath(MavenProject project, PluginDescriptor descriptor) {
        try {
            List<String> runtimeClasspathElements = project.getRuntimeClasspathElements();
            ClassRealm realm = descriptor.getClassRealm();

            for (String element : runtimeClasspathElements) {
                File elementFile = new File(element);
                realm.addURL(elementFile.toURI().toURL());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred during loading project classes to a plugin runtime environment", e);
        }
    }

    public static Object getInstanceFromClazz(String clazzName) {
        return getInstanceFromClazz(clazzName, Object.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getInstanceFromClazz(String clazzName, Class<T> expectedClazz) {
        try {
            Class<?> clazz = Class.forName(clazzName);
            return (T) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Invalid expected class", e);
        }
    }

}
