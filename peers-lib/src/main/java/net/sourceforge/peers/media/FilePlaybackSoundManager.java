package net.sourceforge.peers.media;

import net.sourceforge.peers.Logger;

public class FilePlaybackSoundManager extends AbstractSoundManager {

    private String fileName;
    private DataFormat fileDataFormat;
    private Logger logger;

    private FileReader fileReader;

    public FilePlaybackSoundManager(String fileName, DataFormat fileDataFormat, Logger logger) {
        this.fileName = fileName;
        this.fileDataFormat = fileDataFormat;
        this.logger = logger;
    }

    @Override
    public void init() {
        fileReader = new FileReader(fileName, fileDataFormat, logger);
    }

    @Override
    public void close() {
        if (fileReader != null) fileReader.close();
    }

    @Override
    public int writeData(byte[] buffer, int offset, int length) {
        return 0;
    }

    @Override
    public DataFormat dataProduced() {
        return fileDataFormat;
    }

    @Override
    public byte[] readData() {
        return fileReader.readData();
    }

    @Override
    public boolean finished() {
        return fileReader.finished();
    }

    @Override
    public void waitFinished() throws InterruptedException {
        fileReader.waitFinished();
    }
}
