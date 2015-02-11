package snap.web;
import java.util.*;
import snap.websites.*;

/**
 * This class manages data site.
 */
public class Web {

    // A map of existing WebSites
    Map <WebURL, WebSite>  _sites = Collections.synchronizedMap(new HashMap());

    // The shared web
    static Web  _shared = new Web();
    
/**
 * Returns the shared web.
 */
public static Web getWeb()  { return _shared; }

/**
 * Returns whether a site is already set for URL.
 */
public boolean isSiteSet(WebURL aURL)  { return _sites.get(aURL.getString())!=null; }

/**
 * Returns a site for given source URL.
 */
protected synchronized WebSite getSite(WebURL aSiteURL)
{
    WebSite site = _sites.get(aSiteURL);
    if(site==null) _sites.put(aSiteURL, site = createSite(aSiteURL));
    return site;
}

/**
 * Creates a site for given URL.
 */
protected WebSite createSite(WebURL aSiteURL)
{
    WebURL parentSiteURL = aSiteURL.getSiteURL();
    String scheme = aSiteURL.getScheme(), path = aSiteURL.getPath();
    WebSite site = null;
    
    // If url has path, see if it's jar or zip
    if(path!=null && (path.endsWith(".jar") || path.endsWith(".jar.pack.gz"))) site = new JarFileSite();
    else if(path!=null && path.endsWith(".zip")) site = new ZipFileSite();
    else if(path!=null && parentSiteURL!=null && parentSiteURL.getPath()!=null) site = new DirSite();
    else if(scheme.equals("file")) site = new FileSite();
    else if(scheme.equals("http")) site = new HTTPSite();
    else if(scheme.equals("ftp")) site = new FTPSite();
    else if(scheme.equals("class")) site = new ClassSite();
    else if(scheme.equals("local")) site = new LocalSite();
    else if(scheme.equals("sandbox")) site = new SandboxSite();
    if(site!=null) site.setURL(aSiteURL);
    return site;
}

}