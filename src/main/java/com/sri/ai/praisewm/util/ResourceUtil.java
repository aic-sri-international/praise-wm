package com.sri.ai.praisewm.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * list resources available from the classpath
 *
 * <p>See:
 * https://stackoverflow.com/questions/3923129/get-a-list-of-resources-from-classpath-directory
 */
public class ResourceUtil {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceUtil.class);

  /**
   * For all elements of java.class.path get a Collection of resources Pattern pattern =
   * Pattern.compile(".*"); gets all resources
   *
   * @param pattern the pattern to match
   * @return the resources in the order they are found
   */
  public static Collection<String> getResources(final Pattern pattern) {
    final ArrayList<String> retval = new ArrayList<>();
    final String classPath = System.getProperty("java.class.path", ".");
    final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
    for (final String element : classPathElements) {
      retval.addAll(getResources(element, pattern));
    }
    return retval;
  }

  /**
   * Get a list of names of resources whose full paths match a regular expression.
   *
   * @param regex the regular expression to match
   * @return the resources in the order they are found
   */
  public static List<String> getResourceNames(String regex) {
    return ResourceUtil.getResources(Pattern.compile(regex)).stream()
        .map(e -> new File(e).getName())
        .filter(e -> !e.isBlank())
        .collect(Collectors.toList());
  }

  private static Collection<String> getResources(final String element, final Pattern pattern) {
    final ArrayList<String> retval = new ArrayList<>();
    final File file = new File(element);
    if (file.isDirectory()) {
      retval.addAll(getResourcesFromDirectory(file, pattern));
    } else {
      retval.addAll(getResourcesFromJarFile(file, pattern));
    }
    return retval;
  }

  private static Collection<String> getResourcesFromJarFile(
      final File file, final Pattern pattern) {
    final ArrayList<String> retval = new ArrayList<>();
    ZipFile zf;
    try {
      zf = new ZipFile(file);
    } catch (final IOException e) {
      // There's a problem with the jar, display an info msg and skip it
      LOG.debug("Cannot read jar file: {}, {}. File Skipped", file, e.getMessage());
      return Collections.emptyList();
    }
    final Enumeration e = zf.entries();
    while (e.hasMoreElements()) {
      final ZipEntry ze = (ZipEntry) e.nextElement();
      final String fileName = ze.getName();
      final boolean accept = pattern.matcher(fileName).matches();
      if (accept) {
        retval.add(fileName);
      }
    }
    try {
      zf.close();
    } catch (final IOException e1) {
      throw new Error(e1);
    }
    return retval;
  }

  private static Collection<String> getResourcesFromDirectory(
      final File directory, final Pattern pattern) {
    final ArrayList<String> retval = new ArrayList<>();
    final File[] fileList = directory.listFiles();
    if (fileList == null) {
      return Collections.emptyList();
    }
    for (final File file : fileList) {
      if (file.isDirectory()) {
        retval.addAll(getResourcesFromDirectory(file, pattern));
      } else {
        try {
          final String fileName = file.getCanonicalPath();
          final boolean accept = pattern.matcher(fileName).matches();
          if (accept) {
            retval.add(fileName);
          }
        } catch (final IOException e) {
          throw new Error(e);
        }
      }
    }
    return retval;
  }

  /**
   * list the resources that match args[0]
   *
   * @param args args[0] is the pattern to match, or list all resources if there are no args
   */
  public static void main(final String[] args) {
    Pattern pattern;
    if (args.length < 1) {
      pattern = Pattern.compile(".*");
    } else {
      pattern = Pattern.compile(args[0]);
    }
    final Collection<String> list = ResourceUtil.getResources(pattern);
    System.out.println("ResourceUtil.getResources");
    for (final String name : list) {
      System.out.println(name);
    }
  }
}
