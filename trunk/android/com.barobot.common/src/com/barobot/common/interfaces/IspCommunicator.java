package com.barobot.common.interfaces;

public interface IspCommunicator {
    public boolean open();
    public boolean close();
    public int read(byte[] buf, int size);
    public int write(byte[] buf, int size);
    public boolean isOpened();
    public void clearBuffer();
    public void reset(boolean b);
	public void destroy();
}
