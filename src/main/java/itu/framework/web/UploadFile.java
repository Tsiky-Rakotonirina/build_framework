package itu.framework.web;

public class UploadFile {
    private String filename;
    private String extension;
    private String mimeType;
    private byte[] content;

    public UploadFile() {
    }

    public UploadFile(String filename, String extension, String mimeType, byte[] content) {
        this.filename = filename;
        this.extension = extension;
        this.mimeType = mimeType;
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
