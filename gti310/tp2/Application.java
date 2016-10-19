package gti310.tp2;

import gti310.tp2.audio.AudioFilter;
import gti310.tp2.audio.Convert44100HzTo8000HzAudioFIlter;


public class Application {

	/**
	 * Launch the application
	 * @param args This parameter is ignored
	 */
	public static void main(String args[]) {
		System.out.println("Audio Resample project!");
		
		//Starts the frequency convertion with the 2 paths
		AudioFilter filter = 
				new Convert44100HzTo8000HzAudioFIlter(args[0],args[1]);
	}
}
