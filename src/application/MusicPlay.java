package application;
import java.io.File;
// 文件名:MuiscPlay.java
import java.io.IOException;

import javax.media.NoPlayerException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/*
 * 
 */

public class MusicPlay {
	
    public static void playOnce(String musicName) throws NoPlayerException, IOException, 
    InterruptedException, UnsupportedAudioFileException, IOException, LineUnavailableException {
    	String musicFile = System.getProperty("user.dir") + "\\music\\" + musicName;
//    	System.out.println(musicFile);
  // 获取音频输入流
    	AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
    			new File(FilePathUtil.getFilePath(musicFile)));
  // 获取音频编码对象
    	AudioFormat audioFormat = audioInputStream.getFormat();

  // 设置数据输入
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class,
	    audioFormat, AudioSystem.NOT_SPECIFIED);
		SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
		sourceDataLine.open(audioFormat);
		sourceDataLine.start();
	
  /*
   * 从输入流中读取数据发送到混音器
   */
		int count;
		byte tempBuffer[] = new byte[1024];
		while ((count = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
			if (count > 0) {
				sourceDataLine.write(tempBuffer, 0, count);
			}
		}

  // 清空数据缓冲,并关闭输入
		sourceDataLine.drain();
		sourceDataLine.close();
    }

    public static void main(String args[]) throws NoPlayerException, IOException, InterruptedException,
    	UnsupportedAudioFileException, LineUnavailableException {
    	playOnce("Devils_Never_Cry.wav");

    }
}