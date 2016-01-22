
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Vector;



public class CRBCaptchaTrainer
{
	public CRBCaptchaCrack ccrack;
	public CRBClassifier ccl;
	public Hashtable<String,CRBCaptchaTrainerEntry> htEntry;
	public Vector<CRBCaptchaTrainerEntry> vEntry;
	
	public CRBCaptchaTrainer()
	{
		htEntry = new Hashtable<String,CRBCaptchaTrainerEntry>();
		vEntry = new Vector<CRBCaptchaTrainerEntry>();
	};
	
	public int loadDict()
	{
		ccl = new CRBClassifier();
		ccl.loadDict();
		
		Vector<CRBDictEntry> dict = ccl.getDict();
		BufferedImage img;
		String eq;
		for ( CRBDictEntry de : dict ) {
			img = de.getImage();
			eq = de.getEquiv();
			CRBCaptchaTrainerEntry te = new CRBCaptchaTrainerEntry( img.getWidth(), img.getHeight() );
			te.setEquiv( eq );
			vEntry.add( te );
			htEntry.put( eq, te );
		}
		
		return 0;
	};
	
	public int procImage( byte[] img, String fname )
	{
		ccrack = new CRBCaptchaCrack();
		ccrack.decodeImage( img );
		ccrack.normalize();
		ccrack.partition();
		
		if ( ccrack.lblk.size() != 6 ) {
			// Too bad...
			return -1;
		}
		
		int i;
		CRBCaptchaTrainerEntry te;
		String srv;
		for ( i = 0; i < 6; i++ ) {
			srv = ccl.classifyByMinScore( ccrack.lblk.get( i ) );
			if ( ccl.mscore > 10 ) {
				System.out.printf( "%s-%d: Score %d\n", fname, i+1, ccl.mscore );
			}
			
			te = htEntry.get( srv );
			if ( te == null ) {
				System.out.printf( "Key not found: %s\n", srv );
			} else {
				te.addImage( ccrack.lblk.get( i ), ccl.ic.box, ccl.ic.boy );
			}
		}
		
		return 0;
	};
	
	public int writeAll()
	{
		int rv;
		for ( CRBCaptchaTrainerEntry te : vEntry ) {
			System.out.printf( "Entry %s: %d copies\n", te.equiv, te.imgcount );
			rv = te.formImage();
			if ( rv < 0 ) continue;
			te.writeImage( "-orig" );
			te.recreateImage();
			te.writeImage( "" );
		}
		
		return 0;
	};
	
	public static void main( String[] args )
	{
		CRBCaptchaTrainer ct = new CRBCaptchaTrainer();
		
		ct.loadDict();
		
		int i;
		File fi;
		FileInputStream fis;
		byte[] data;
		int off, nr;
		String fname;
		for ( i = 0; i < 1000; i++ ) {
			fname = "data" + String.valueOf( i+1 ) + ".png";
			try {
				fi = new File( fname );
				fis = new FileInputStream( fi );
			} catch( Exception e ) {
				e.printStackTrace();
				return;
			}
			
			data = new byte[(int)fi.length()];

			off = 0;
			nr = 0;
			try {
				while ( (off<data.length) && ( (nr=fis.read(data, off, data.length-off)) >= 0 ) ) {
					off += nr;
				}
				fis.close();
			} catch ( Exception e ) {
				e.printStackTrace();
				return;
			}
			
			ct.procImage( data, fname );
			System.out.printf( "Done with image %d\n", i+1 );
		}
		
		ct.writeAll();
	};
};
