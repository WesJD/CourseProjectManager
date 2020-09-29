package edu.umd.cs.eclipse.courseProjectManager;

import java.util.*;
import java.io.*;

class SubmitIgnoreFilter
{
    private final Collection<String> filters;
    private static final String[] DEFAULTS;
    
    static {
        DEFAULTS = new String[] { ".", "..", "core", "RCSLOG", "tags", "TAGS", "RCS", "SCCS", ".make.state", ".nse_depinfo", "#*", ".#*", "cvslog.*", ",*", ".git", "CVS", "CVS.adm", ".del-*", "*.a", "*.olb", "*.o", "*.obj", "*.so", "*.Z", "*~", "*.old", "*.elc", "*.ln", "*.bak", "*.BAK", "*.orig", "*.rej", "*.exe", "*.dll", "*.pdb", "*.lib", "*.ncb", "*.ilk", "*.exp", "*.suo", ".DS_Store", "_$*", "*$", "*.lo", "*.pch", "*.idb", "*.class", "~*" };
    }
    
    SubmitIgnoreFilter() {
        this.filters = new LinkedHashSet<String>();
        String[] defaults;
        for (int length = (defaults = SubmitIgnoreFilter.DEFAULTS).length, i = 0; i < length; ++i) {
            final String p = defaults[i];
            this.addFilter(p);
        }
    }
    
    void addFilter(String filterString) {
        filterString = filterString.replace("$", "\\$");
        filterString = filterString.replace(".", "\\.");
        filterString = filterString.replace("*", ".*");
        filterString = "^(.*/)*" + filterString;
        this.filters.add(filterString);
    }
    
    boolean matches(final String filename) {
        for (final String regexp : this.filters) {
            if (filename.matches(regexp)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        final StringBuffer result = new StringBuffer();
        final Iterator<String> ii = this.filters.iterator();
        while (ii.hasNext()) {
            result.append(String.valueOf(ii.next()) + "\n");
        }
        return result.toString();
    }
    
    Iterator<String> iterator() {
        return this.filters.iterator();
    }
    
    static SubmitIgnoreFilter createSubmitIgnoreFilterFromFile(final String filename) throws IOException {
        final SubmitIgnoreFilter submitIgnoreFilter = new SubmitIgnoreFilter();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String filterString;
            while ((filterString = TurninProjectAction.readLine(reader)) != null) {
                submitIgnoreFilter.addFilter(filterString);
            }
            return submitIgnoreFilter;
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
