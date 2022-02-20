package esql.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;

public class ValueCLOB extends ValueLOB {

    private final char[] first_in_mem;
    private final Path tempFile;
    final boolean national;
    final long stringLen;

    public ValueCLOB(Path tempFile, char[] first_in_mem, long lobSize, long stringLen, boolean national, byte[]... preHash) {
        super(lobSize, preHash);
        this.first_in_mem = first_in_mem;
        this.tempFile = tempFile;
        this.stringLen = stringLen;
        this.national = national;
    }
    @Override
    public boolean isNull() {
        return this.first_in_mem == null;
    }

    @Override
    public boolean isTrue() {
        return !isEmpty();
    }

    @Override
    public Value convertTo(Types type) {
        return null;
    }

    @Override
    public Types getType() {
        return national ? Types.TYPE_NCLOB : Types.TYPE_CLOB;
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if(tempFile != null && Files.exists(tempFile)) {
            //don't care about Charset, provides as is.
            return Files.newInputStream(tempFile, StandardOpenOption.READ);
        }
        if(lobSize > first_in_mem.length) {
            throw new IllegalStateException("Temp file gone, in_memory data does not have full data length.");
        }
        byte[] t_array = ValueString.STRING_CONVERT_CHARSET.encode(CharBuffer.wrap(first_in_mem)).array();
        return new ByteArrayInputStream(t_array);
    }

    @Override
    public byte[] ensureHash(int algo) throws IOException, NoSuchAlgorithmException {
        return new byte[0];
    }

    @Override
    Path getTempFile() {
        return tempFile;
    }

    @Override
    public int compareTo(Value o) {
        return 0;
    }

    @Override
    public String toString() {
        if(lobSize> first_in_mem.length)
            return (national?"(nclob=":"(clob=")+stringLen+"chars,"+lobSize+")"+String.valueOf(first_in_mem)+" remain "+(stringLen- first_in_mem.length)+"chars";
        return (national?"(nclob=":"(clob=")+stringLen+"chars,"+lobSize+")"+String.valueOf(first_in_mem);
    }

    public long stringLength() {
        return stringLen;
    }

    @Override
    public void close() throws IOException {
        if(tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }
}
