package com.atypon.nosqldbserver.interceptor;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

public class BufferedServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] buffer;

    public BufferedServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        InputStream inputStream = request.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int read;
        while ((read = inputStream.read(buff)) > 0) {
            outputStream.write(buff, 0, read);
        }
        this.buffer = outputStream.toByteArray();
    }

    @Override
    public ServletInputStream getInputStream() {
        return new BufferedServletInputStream(this.buffer);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }
}
