package gti310.tp2.audio;

/**
 * Concrete Audio Filter that only modifies 44,1 kHz audio files
 * 
 * @author Alexis Michaud
 *
 */
public class Convert44100HzTo8000HzAudioFIlter implements AudioFilter 
{
	
	//local attributes
	private String entryPath;
	private String exitPath;
	
	//Constructor
	public Convert44100HzTo8000HzAudioFIlter(String entryPath, 
			String exitPath) 
	{
		entryPath = this.entryPath;
		exitPath = this.exitPath;
	}

	/**
	 * Filter the input data.
	 * The function makes sure the input data is valid beforehand.
	 */
	public void process() 
	{
		//TODO ENDIAN!!!!! only flip little endian
		
		//Verify if wave
		//Verify if 44,1kHz
		//Keep popped information in byte entete[]
		
		//8bits or 16bits
		//stereo or mono 
		
		//get samples
		
		//convert samples
		
		
		
	}
}
