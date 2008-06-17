package org.contineo.core.transfer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.contineo.core.security.Menu;

/**
 * @author Administrator
 */
public interface Export {
    
    ByteArrayOutputStream process(Menu menu, String user) throws IOException;

    ByteArrayOutputStream process(int menuId, String user) throws IOException;
}
