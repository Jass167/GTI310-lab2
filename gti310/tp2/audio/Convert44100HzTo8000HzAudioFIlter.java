package gti310.tp2.audio;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
	private int initialSubchunk2Size = 0;
	private int initialNumSamples = 0;
	byte[] initialAudioData = null;
	/*le nombre 1463899717 est sorti de la reference 
	file:///C:/Users/TEMP/Desktop/media/Microsoft%20WAVE%20soundfile%20format.htm
	le mot WAVE en exadecimal est   (0x57415645 big-endian form). 
	donc on a change en decimal
	 */
	private static final int WAVEHEADER = 1463899717;
	private static final int INITIALSAMPLERATE = 44100;	
	private static final int NEWSAMPLERATE = 8000;

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
		// TODO pas OUBLIER DE METTRE LA NOTATION O() dans les algoritmes 

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
			//Donc 4 scenarios possibles 

			//8bits et mono
			if(traiteurHeader.getBitParSample()==8 
					&& traiteurHeader.getIsStereo()==1){
				//on va convertir les données en consequence 
				newData=dataRateConverter(numberOfSamples,8,1);

				//on update le nouveau header en consequence 
				newHeader= traiteurHeader.updateDuNewHeader(8,1,newNumberOfSamples);


			}
			//8bits et stereo
			else if(traiteurHeader.getBitParSample()==8 && traiteurHeader.getIsStereo()==2){

				newData= dataRateConverter(numberOfSamples,8,2);

				//on update le nouveau header en consequence 
				newHeader =traiteurHeader.updateDuNewHeader(8,2,newNumberOfSamples);

			}
			//16bits et mono
			else if(traiteurHeader.getBitParSample()==16 && traiteurHeader.getIsStereo()==1){


				newData=dataRateConverter(numberOfSamples,16,1);

				//on update le nouveau header en consequence 
				newHeader =traiteurHeader.updateDuNewHeader(16,1,newNumberOfSamples);

			}
			//16bits et stereo
			else if(traiteurHeader.getBitParSample()==16 && traiteurHeader.getIsStereo()==2){

				newData=dataRateConverter(numberOfSamples,16,2);

				//on update le nouveau header en consequence 
				newHeader= traiteurHeader.updateDuNewHeader(16,1,newNumberOfSamples);

			}
			//Gérer si le .wav n'est pas 8 ou 16 BPS ou mono/stereo
			else
			{
				System.out.println("The initial wavefile isn't valid");
			}


			/*
			 * Création du nouveau fichier .wav avec le nouveau sample rate
			 */
			try {

				this.fileSink = new FileSink(exitPath);

				fileSink.push(newHeader);
				fileSink.push(newData);	

				System.out.println("La conversion du WAVE de "+
						traiteurHeader.getIsStereo()+" channels et "+
						traiteurHeader.getBitParSample()+" bits a reussi!! ");

				fileSource.close();			
				fileSink.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 *Méthode qui converti le data rate d'un sample
	 *Pour le lab, on converti de 44,1kHz a 8kHz
	 */
	private byte[] dataRateConverter(int numberOfSamples,int bitParSample, int isStereo)
	{
		//get le nombre de data du .wav initial
		initialSubchunk2Size = traiteurHeader.getChunk2Size();

		//pop le reste des données du file source avec le initialSubchunk2Size
		initialAudioData = fileSource.pop(initialSubchunk2Size);

		//get le nombre d'échantillons du .wav initial
		//Subchunk2Size    == NumSamples * NumChannels * BitsPerSample/8
		initialNumSamples = initialSubchunk2Size / ( isStereo * bitParSample);

		/*Trouver le nombre d'échantillons prélevés qui reconstruiront le .wav
	    Ce nombre correspond a la fraction entre les frequences 
	    dechantillonage. Exemple : a chaque 44100 echantillons, on veut en
	    prélever seulement 8000 donc le nombre déchantillons sera celui 
	    initial x 8000/44100 */
		newNumberOfSamples = 
				( NEWSAMPLERATE / INITIALSAMPLERATE )* initialNumSamples;


		//Stock les nouveaux échantillons dans le tableau a retourner
		byte[] convertedData = new byte[newNumberOfSamples];


		//traiter quelle information on met dans convertedData selon 
		//les 4 scenarios

		//mono 8 bits
		if(isStereo == 1 && bitParSample == 8)
			for( int i = 0 ; i <= newNumberOfSamples ; i++)
			{
				/* L'index est multiplié par le facteur de convertion
				 * pour savoir quelles valeurs de initialAudioData 
				 * nous allons utiliser pour la conversion
				 */
				double interpolationIndexValue = i * ( INITIALSAMPLERATE / NEWSAMPLERATE );

				//les index d'un tableau doivent etre des int mais on a besoin 
				//de prendre les décimales en compte pour l'interpolation
				if (interpolationIndexValue%1 != 0)
				{
					//interpolation
					//convertedData[i] = 
					double interpolValue = 
							interpolationIndexValue%1 * 
							java.lang.Math.abs(initialAudioData[(int)interpolationIndexValue+1]
									- initialAudioData[(int)interpolationIndexValue]);

					//variable temporaire contenant le data en int qui sera
					//transformé en byte
					int convertedDataInt = 0;

					//on gere differents scenarios en fonction des valeurs
					//avec lesquelles on fait linterpolation
					if(initialAudioData[(int)interpolationIndexValue] > 
					initialAudioData[(int)interpolationIndexValue+1])
					{
						convertedDataInt = (int)
								(initialAudioData[(int)interpolationIndexValue] 
										- interpolValue );

						convertedData[i] = intToByte(convertedDataInt);
					}
					else if (initialAudioData[(int)interpolationIndexValue] < 
							initialAudioData[(int)interpolationIndexValue+1])
					{
						convertedDataInt = (int)
								(initialAudioData[(int)interpolationIndexValue] 
										+ interpolValue );

						convertedData[i] = intToByte(convertedDataInt);
					}
					else //if(initialAudioData[(int)interpolationIndexValue] = 
						//initialAudioData[(int)interpolationIndexValue+1])
					{
						convertedDataInt = (int)
								(initialAudioData[(int)interpolationIndexValue]);
						convertedData[i] = intToByte(convertedDataInt);
					}
				}
				
				else if (interpolationIndexValue%1 == 0)
				{
					convertedData[i] = initialAudioData[(int)interpolationIndexValue];
				}
				
				else
				{
					System.out.println("Invalid sampling rates");
				}
			}

		//mono 16 bits
		if(isStereo == 1 && bitParSample == 8)
		{}

		//stereo 8 bits
		if(isStereo == 2 && bitParSample == 16)
		{}

		//stereo 16 bits
		if(isStereo == 2 && bitParSample == 16)
		{}



		return convertedData;
	}

	/** This method flips the order of the bytes from little Endian to a big
	 * Endian order 
	 * @param toBeFlipped is the little endian content that needs to be flipped
	 * @return the data in big Endian order.
	 * */
	private byte[] littleEndianDataFlip(byte[] toBeFlipped)
	{
		//the new data array is instanciated with the proper length
		byte[] flippedData = new byte[toBeFlipped.length];

		//flips the data order
		for( int  i=0 ; i < toBeFlipped.length ; i++)
		{
			flippedData[i] = toBeFlipped[toBeFlipped.length - 1 - i];
		}

		return flippedData;
	}

	//Converts an integer to a byte
	private byte intToByte(int integerToConvert)
	{
		byte[] convertedByte =
				ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(integerToConvert).array();
		return convertedByte[0];
	}
}
