package com.decibel.uasparser;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import jregex.Matcher;
import jregex.Pattern;

/**
 * This parser implementation is not thread-safe as it re-uses Matcher objects instead of creating
 * them on each call to parse() for a modest speedup.
 *
 * Recommended for single-threaded scenarios, such as Hadoop map/reduce jobs.
 *
 * @author chetan
 *
 */
public class SingleThreadedUASparser extends UASparser {

    protected Map<Matcher, Long> compiledBrowserMatcherMap;
    protected Map<Matcher, Long> compiledOsMatcherMap;
    protected Map<Matcher, Long> compiledDeviceMatcherMap;

    public SingleThreadedUASparser() {
    }
    
    public SingleThreadedUASparser(InputStream inputStreamToDefinitionFile) throws IOException {
        super(inputStreamToDefinitionFile);
    }

    public SingleThreadedUASparser(String localDefinitionFilename) throws IOException {
        super(localDefinitionFilename);
    }

    /**
     * Precompile browser regexes
     */
    @Override
    protected void preCompileBrowserRegMap() {
        this.compiledBrowserMatcherMap = preCompileBrowserMatcherMap();
    }

    protected LinkedHashMap<Matcher, Long> preCompileBrowserMatcherMap() {
        LinkedHashMap<Matcher, Long> compiledBrowserMatcherMap =
                new LinkedHashMap<Matcher, Long>(browserRegMap.size());

        for (Map.Entry<String, Long> entry : browserRegMap.entrySet()) {
            Pattern pattern = new Pattern(entry.getKey(), Pattern.IGNORE_CASE | Pattern.DOTALL);
            compiledBrowserMatcherMap.put(pattern.matcher(), entry.getValue());
        }
        return compiledBrowserMatcherMap;
    }

    /**
     * Precompile OS regexes
     */
    @Override
    protected void preCompileOsRegMap() {
        this.compiledOsMatcherMap = preCompileOsMatcherMap();
    }

    protected LinkedHashMap<Matcher, Long> preCompileOsMatcherMap() {
        LinkedHashMap<Matcher, Long> compiledOsMatcherMap =
                new LinkedHashMap<Matcher, Long>(osRegMap.size());

        for (Map.Entry<String, Long> entry : osRegMap.entrySet()) {
            Pattern pattern = new Pattern(entry.getKey(), Pattern.IGNORE_CASE | Pattern.DOTALL);
            compiledOsMatcherMap.put(pattern.matcher(), entry.getValue());
        }
        return compiledOsMatcherMap;
    }

    /**
     * Precompile device regexes
     */
    @Override
    protected void preCompileDeviceRegMap() {
        this.compiledDeviceMatcherMap = preCompileDeviceMatcherMap();
    }

    protected LinkedHashMap<Matcher, Long> preCompileDeviceMatcherMap() {
        if (deviceRegMap == null) {
            return null; // skip for older ini files
        }
        LinkedHashMap<Matcher, Long> compiledDeviceMatcherMap =
                new LinkedHashMap<Matcher, Long>(deviceRegMap.size());

        for (Map.Entry<String, Long> entry : deviceRegMap.entrySet()) {
            Pattern pattern = new Pattern(entry.getKey(), Pattern.IGNORE_CASE | Pattern.DOTALL);
            compiledDeviceMatcherMap.put(pattern.matcher(), entry.getValue());
        }
        return compiledDeviceMatcherMap;
    }

    /**
     * Searches in the os regex table. if found a match copies the os data
     *
     * @param useragent
     * @param retObj
     */
    @Override
    protected void processOsRegex(String useragent, UserAgentInfo retObj) {
        Set<Entry<Matcher, Long>> osMatcherSet = getOsMatcherSet();
        for (Map.Entry<Matcher, Long> entry : osMatcherSet) {
            Matcher matcher = entry.getKey();
            matcher.setTarget(useragent);
            if (matcher.find()) {
                retObj.setOsEntry(osMap.get(entry.getValue()));
                break;
            }
        }
    }

    /**
     * Searchs in the browser regex table. if found a match copies the browser data and if possible os data
     *
     * @param useragent
     * @param retObj
     */
    @Override
    protected void processBrowserRegex(String useragent, UserAgentInfo retObj) {
        Set<Entry<Matcher, Long>> browserMatcherSet = getBrowserMatcherSet();
        for (Map.Entry<Matcher, Long> entry : browserMatcherSet) {
            Matcher matcher = entry.getKey();
            matcher.setTarget(useragent);
            if (matcher.find()) {
                Long idBrowser = entry.getValue();
                BrowserEntry be = browserMap.get(idBrowser);
                if (be != null) {
                    retObj.setType(browserTypeMap.get(be.getType()));;
                    if (matcher.groupCount() > 1) {
                        retObj.setBrowserVersionInfo(matcher.group(1));
                    }
                    retObj.setBrowserEntry(be);
                }
                // check if this browser has exactly one OS mapped
                Long idOs = browserOsMap.get(idBrowser);
                if (idOs != null) {
                    retObj.setOsEntry(osMap.get(idOs));
                }
                return;
            }
        }
    }

    /**
     * Searches in the devices regex table. if found a match copies the device data
     *
     * @param useragent
     * @param uaInfo
     */
    @Override
    protected void processDeviceRegex(String useragent, UserAgentInfo uaInfo) {
        Set<Entry<Matcher, Long>> deviceMatcherSet = getDeviceMatcherSet();
        if (deviceMatcherSet == null || deviceMap == null) {
            return;
        }
        for (Map.Entry<Matcher, Long> entry : deviceMatcherSet) {
            Matcher matcher = entry.getKey();
            matcher.setTarget(useragent);
            if (matcher.find()) {
                uaInfo.setDeviceEntry(deviceMap.get(entry.getValue()));
                return;
            }
        }
    }

    protected Set<Entry<Matcher, Long>> getOsMatcherSet() {
        return compiledOsMatcherMap.entrySet();
    }

    protected Set<Entry<Matcher, Long>> getBrowserMatcherSet() {
        return compiledBrowserMatcherMap.entrySet();
    }

    protected Set<Entry<Matcher, Long>> getDeviceMatcherSet() {
        if (compiledDeviceMatcherMap == null) {
            return null;
        }
        return compiledDeviceMatcherMap.entrySet();
    }

}
