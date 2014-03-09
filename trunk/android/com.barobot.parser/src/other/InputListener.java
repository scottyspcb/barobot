package other;

public interface InputListener {
	void onNewData(byte[] data);
	void onRunError(Exception e);
}
