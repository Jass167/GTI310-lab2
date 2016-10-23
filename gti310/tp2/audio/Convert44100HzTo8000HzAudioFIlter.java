package gti310.tp2.audio;

import java.io.FileNotFoundException;

import gti310.tp2.io.FileSink;
import gti310.tp2.io.FileSource;

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
	private FileSource fileSource = null;
	private FileSink fileSink = null;
	private TraiteurHeader traiteurHeader = null;
	private int newNumberOfSamples = 0;
	private byte[] newHeader = null; 
	private byte[] newData = null; 
	/*le nombre 1463899717 est sorti de la reference 
	file:///C:/Users/TEMP/Desktop/media/Microsoft%20WAVE%20soundfile%20format.htm
	le mot WAVE en exadecimal est   (0x57415645 big-endian form). 
	donc on a change en decimal
	 */
	private static final int WAVEHEADER = 1463899717;
	private static final int INITIALSAMPLERATE = 44100;

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
		// pas OUBLIER DE METTRE LA NOTATION O() dans les algoritmes 

		// creer le new fileSource
		try {
			this.fileSource = new FileSource(entryPath);

		} catch (FileNotFoundException e) {
			//Auto-generated catch block
			e.printStackTrace();
		}

		//on pop le header au complet pour le traiter dans la classe TraiteurHeader
		byte[] headerEnBrut = fileSource.pop(44);
		this.traiteurHeader = new TraiteurHeader(headerEnBrut);

		/*Verify if wave*/
		 
		if (traiteurHeader.getCheckWave() != WAVEHEADER){
			fileSource.close();
			System.out.println("Le fichier n'est pas un WAVE file");
		}
		//Verify if 44,1kHz
		else if (traiteurHeader.getSampleRate() != INITIALSAMPLERATE) 
		{
			fileSource.close();
			System.out.println("Le fichier n'a pas un sample rate de " + 
			INITIALSAMPLERATE + " Hz");
		}
		/*si le fichier est un WAVE et il est a 44100HZ on va transformer 
		  le file et on va updater le header*/

		else{

			/*Subchunk2Size == NumSamples * NumChannels * BitsPerSample/8
            This is the number of bytes in the data.
            You can also think of this as the size
            of the read of the subchunk following this 
            number.
            */
			int numberOfSamples= traiteurHeader.getChunk2Size()/(traiteurHeader.getIsStereo()*(traiteurHeader.getBitParSample()/8));
			
		
			//8bits or 16bits ET stereo or mono
			//Donc 4 ecenario possibles 

			//8bits et mono
			if(traiteurHeader.getBitParSample()==8 
					&& traiteurHeader.getIsStereo()==1){
				//on va convertir les Donnes data en consequence 
				newNumberOfSamples = getNewNumberOfSamples(numberOfSamples,8,1);
				newData=converteurData(numberOfSamples,8,1);
		
				//on update le nouveau header en consequence 
				newHeader= traiteurHeader.updateDuNewHeader(8,1,newNumberOfSamples);
						
				
			}
			//8bits et stereo
			if(traiteurHeader.getBitParSample()==8 && traiteurHeader.getIsStereo()==2){
				
				 newNumberOfSamples = getNewNumberOfSamples(numberOfSamples,8,2);
				newData= converteurData(numberOfSamples,8,2);
				
				//on update le nouveau header en consequence 
				newHeader =traiteurHeader.updateDuNewHeader(8,2,newNumberOfSamples);
				
			}
			//16bits et mono
			if(traiteurHeader.getBitParSample()==16 && traiteurHeader.getIsStereo()==1){
				
				newNumberOfSamples = getNewNumberOfSamples(numberOfSamples,16,1);
				newData=converteurData(numberOfSamples,16,1);
				
				//on update le nouveau header en consequence 
				newHeader =traiteurHeader.updateDuNewHeader(16,1,newNumberOfSamples);
				
			}
			//16bits et stereo
			if(traiteurHeader.getBitParSample()==16 && traiteurHeader.getIsStereo()==2){
				
				newNumberOfSamples = getNewNumberOfSamples(numberOfSamples,16,1);
				newData=converteurData(numberOfSamples,16,2);
				
				//on update le nouveau header en consequence 
				newHeader= traiteurHeader.updateDuNewHeader(16,1,newNumberOfSamples);
				
			}
			
			try {
				
				this.fileSink = new FileSink(exitPath);
				
                fileSink.push(newHeader);
                fileSink.push(newData);	
                
                System.out.println("La conversion du WAVE de "+ traiteurHeader.getIsStereo()+" chanels et "+traiteurHeader.getBitParSample()+" bits a reussi!! ");
                
                fileSource.close();			
                fileSink.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}

	
	}
	
	private int getNewNumberOfSamples(int numberOfSamples,int bitParSample ,int isStereo ) {
		// TODO Auto-generated method stub
		return 0;
	}

	//Convertir samples
	private byte[] converteurData(int numberOfSamples,int bitParSample, int isStereo){
		
		 byte[] new8kHzData = new byte[newNumberOfSamples];
		 
		
		return new8kHzData;
	}
}
