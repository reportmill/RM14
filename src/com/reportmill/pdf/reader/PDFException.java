/**
 * PDFException.java 
 * Subclass of Error because declaring every method
 * as throwing an exception seems stupid.
 */

package com.reportmill.pdf.reader;

public class PDFException extends Error {

public PDFException() {
    super();
}

public PDFException(String message) {
    super(message);
}

public PDFException(String message, Throwable cause) {
    super(message, cause);
}

public PDFException(Throwable cause) {
    super(cause);
}


}
