package com.example.demo.config;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Response wrapper to prevent "getOutputStream() has already been called" errors
 * Allows buffering of response content before actually writing to client
 */
public class ResponseFilterWrapper extends HttpServletResponseWrapper {

    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private ServletOutputStream streamProxy;
    private PrintWriter writerProxy;
    private boolean getOutputStreamCalled = false;
    private boolean getWriterCalled = false;

    public ResponseFilterWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (getWriterCalled) {
            throw new IllegalStateException("getWriter() has already been called");
        }
        
        if (streamProxy == null) {
            getOutputStreamCalled = true;
            streamProxy = new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    buffer.write(b);
                }

                @Override
                public void flush() throws IOException {
                    buffer.flush();
                }

                @Override
                public void close() throws IOException {
                    buffer.close();
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener listener) {
                    // No-op
                }
            };
        }
        return streamProxy;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (getOutputStreamCalled) {
            throw new IllegalStateException("getOutputStream() has already been called");
        }
        
        if (writerProxy == null) {
            getWriterCalled = true;
            writerProxy = new PrintWriter(buffer, true);
        }
        return writerProxy;
    }

    /**
     * Write buffered content to the actual response
     */
    public void flushBuffer() throws IOException {
        if (writerProxy != null) {
            writerProxy.flush();
        }
        if (streamProxy != null) {
            streamProxy.flush();
        }

        byte[] content = buffer.toByteArray();
        if (content.length > 0) {
            super.getOutputStream().write(content);
            super.getOutputStream().flush();
        }
    }

    /**
     * Get buffered content as bytes
     */
    public byte[] getBufferedContent() {
        return buffer.toByteArray();
    }

    /**
     * Reset the buffer
     */
    public void resetBuffer() {
        buffer.reset();
        getOutputStreamCalled = false;
        getWriterCalled = false;
    }
}
