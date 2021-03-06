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
	private byte[] initialAudioData = null;
	/*le nombre 1463899717 est sorti de la reference 
	file:///C:/Users/TEMP/Desktop/media/Microsoft%20WAVE%20soundfile%20format.htm
	le mot WAVE en exadecimal est   (0x57415645 big-endian form). 
	donc on a change en decimal
	 */
	private static final int WAVEHEADER = 1163280727;
	private static final double INITIALSAMPLERATE = 44100;	
	private static final double NEWSAMPLERATE = 8000;


	//Constructor
	public Convert44100HzTo8000HzAudioFIlter(String entryPath, 
			String exitPath) 
	{
		this.entryPath = entryPath;
		this.exitPath = exitPath;
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
			initialNumSamples = traiteurHeader.getChunk2Size()/
					(traiteurHeader.getIsStereo()*
							(traiteurHeader.getBitParSample()/8));
			
			
			/*Trouver le nombre d'�chantillons pr�lev�s qui reconstruiront le .wav
		    Ce nombre correspond a la fraction entre les frequences 
		    dechantillonage. Exemple : a chaque 44100 echantillons, on veut en
		    pr�lever seulement 8000 donc le nombre d�chantillons sera celui 
		    initial x 8000/44100 */
			newNumberOfSamples = (int)(( NEWSAMPLERATE / INITIALSAMPLERATE )* initialNumSamples);

		

			//pop le reste des donn�es du file source avec le initialSubchunk2Size
			initialAudioData = fileSource.pop(traiteurHeader.getChunk2Size());


			//8bits or 16bits ET stereo or mono
			//Donc 4 scenarios possibles 

			if((traiteurHeader.getBitParSample()==8 ||
					traiteurHeader.getBitParSample()==16) &&
					(traiteurHeader.getIsStereo()==1 || 
					traiteurHeader.getIsStereo()==2)){	
				//on va convertir les donn�es en consequence 
				newData = dataRateConverter(initialAudioData,
						initialNumSamples,traiteurHeader.getBitParSample(),
						traiteurHeader.getIsStereo());


				//on update le nouveau header en consequence 
				newHeader= 
						traiteurHeader.updateDuNewHeader(traiteurHeader.getBitParSample(),
								traiteurHeader.getIsStereo(),newNumberOfSamples);
			}


			//G�rer si le .wav n'est pas 8 ou 16 BPS ou mono/stereo
			else
			{
				System.out.println("Le fichier WAVE n'est pas valide, "
						+ "soit les BPS ou # de chanels  ");
			}



			/*
			 * Cr�ation du nouveau fichier .wav avec le nouveau sample rate
			 */
			try {


				this.fileSink = new FileSink(exitPath);


				fileSink.push(newHeader);
				fileSink.push(newData);	


				System.out.println("Done : La conversion du WAVE initial de "+
						traiteurHeader.getIsStereo()+" channel(s) et "+
						traiteurHeader.getBitParSample()+" bits est reussie!! ");


				fileSource.close();			
				fileSink.close();


			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}


	/*
	 *M�thode qui converti le data rate d'un sample
	 *Pour le lab, on converti de 44,1kHz a 8kHz
	 */
	private byte[] dataRateConverter(byte[] initialAudioData,
			int initialNumSamples,int bitParSample, int isStereo)
	{
		//traiter quelle information on met dans convertedData selon 
		//les 4 scenarios

		//Stock les nouveaux �chantillons dans le tableau a retourner
		byte[] convertedData = null;
		
		//mono 8 bits 
		if(isStereo == 1 && bitParSample == 8)
		{
			//Stock les nouveaux �chantillons dans le tableau a retourner
			convertedData = new byte[newNumberOfSamples];
			
			
			for( int i = 0 ; i < newNumberOfSamples ; i++)
			{
				/* L'index est multipli� par le facteur de conversion
				 * pour savoir quelles valeurs de initialAudioData 
				 * nous allons utiliser pour la conversion
				 **/
				double interpolationIndexValue = i * 
						( INITIALSAMPLERATE / NEWSAMPLERATE );


				//les index d'un tableau doivent etre des int mais on a besoin 
				//de prendre les d�cimales en compte pour l'interpolation

				//si lindex contient des decimale, on interpole
				if (interpolationIndexValue%1 != 0)
				{
					//interpolation

					int deltaY =  
							initialAudioData[((int)interpolationIndexValue)+1]
									- initialAudioData[(int)interpolationIndexValue];
					//�gal toujours 1 car on prend 2 valeurs coll�es dans le tableau de bytes
					int deltaX = 1;



					//variable temporaire contenant le data en int qui sera
					//transform� en byte
					int interpolationResult = 
							(int) ( initialAudioData[(int)interpolationIndexValue]);
						
					/**Avec l'interpolation lin�aire, on ajoutait ces donn�es
					 * � interpolationResult pour calculer nos valeurs,
					 * seulement, le r�sultat obtenu avait plein de bruits
					 * ind�sirables et c'est pourquoi nous avons 'skip' 
					 * l'interpolation:
					+  (deltaY/deltaX)*(interpolationIndexValue - 			
					(int) interpolationIndexValue) ); */
					
					convertedData[i] = (byte) interpolationResult;
				}

				//if the interpolationIndexValue is a round number without
				//decimals, no interpolation needed.
				else if (interpolationIndexValue%1 == 0)
				{
					convertedData[i] =
							initialAudioData[(int)interpolationIndexValue];
				}
			}
		}


		//mono 16 bits
		if(isStereo == 1 && bitParSample == 16)
		{
			//Stock les nouveaux �chantillons dans le tableau a retourner
			convertedData = new byte[newNumberOfSamples*2];	//2 bytes par sample
			
			//utilis� pour update convertedData(on prend 2 echantillons par boucle)
			//on doit utiliser un index diff�rent de i car l'index de convertedData
			//augmente de 2 positions par it�ration.
			int j = 0;
			
			for( int i = 0 ; i < newNumberOfSamples ; i++ )
			{
					
				/* L'index est multipli� par le facteur de conversion
				 * pour savoir quelles valeurs de initialAudioData 
				 * nous allons utiliser pour la conversion
				 **/
				double interpolationIndexValue = i * 
						( INITIALSAMPLERATE / NEWSAMPLERATE );

				//si lindex contient des decimale, on interpole
				if (interpolationIndexValue%1 != 0)
				{
					int deltaY = ( initialAudioData[((int)interpolationIndexValue)*2+3]<<8
							+ initialAudioData[((int)interpolationIndexValue)*2+2] )
							- ( initialAudioData[((int)interpolationIndexValue)*2+1]<<8
									+ initialAudioData[((int)interpolationIndexValue)*2] );

					//�gal toujours 1 car on prend 2 �chantillons coll�es dans
					//le tableau de bytes
					int deltaX = 1;

					double yA = ( initialAudioData[(int)interpolationIndexValue+1]<<8
							+ initialAudioData[(int)interpolationIndexValue] );

					//interpolation
					int interpolationResult = 
							(int)( yA + (deltaY/deltaX)
							* ( (interpolationIndexValue) - 
									(interpolationIndexValue)%1 ));
									


					byte[] interpolationBuffer = 
							ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short)interpolationResult).array();

					convertedData[j] = (byte) interpolationBuffer[0];

					convertedData[j+1] = (byte) interpolationBuffer[1];


				}
				//if the interpolationIndexValue is a round number without
				//decimals, no interpolation needed.
				else if (interpolationIndexValue%1 == 0)
				{					
					convertedData[j] = (initialAudioData[((int)interpolationIndexValue)*2]);
					convertedData[j+1] = (initialAudioData[((int)interpolationIndexValue)*2+1]);
					
				}
				
				//ayant 2 nouvelles valeurs dans convertedData, on augmente l'
				//index de 2
				j+=2;
			}
		}


		//stereo 8 bits
		if(isStereo == 2 && bitParSample == 8)
		{
			
		}


		//stereo 16 bits
		if(isStereo == 2 && bitParSample == 16)
		{
			//TODO endiennn pour 16


		}

		return convertedData;
	}
}