import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;

public class Example
{
	public static void main( String[] argz )
	{
		CRBCaptchaCrack.initClassifier();
		
		int rv;
		int i, j;
		
		if ( argz.length != 1 ) {
			System.out.printf( "Usage: Example <Input Image File>\n" );
			return;
		}
		

		// ----------------- Read the File ----------------
		String fname = argz[0];
		FileInputStream fis;
		File fi;
		try {
			fi = new File( fname );
			fis = new FileInputStream( fi );
		} catch( Exception e ) {
			e.printStackTrace();
			return;
		}

		byte[] data = new byte[(int)fi.length()];

		int off = 0;
		int nr = 0;
		try {
			while ( (off<data.length) && ( (nr=fis.read(data, off, data.length-off)) >= 0 ) ) {
				off += nr;
			}
			fis.close();
		} catch ( Exception e ) {
			e.printStackTrace();
			return;
		}

		// ---------------- Decode Image ------------------
		CRBCaptchaCrack cc = new CRBCaptchaCrack();
		rv = cc.decodeImage(data);
		if ( rv < 0 ) {
			System.out.printf( "Failure to read image!\n" );
		}
		cc.normalize();

		// ------------ Partition and Normalize -----------
		cc.partition();
		cc.classify();
		
		// ----------------- Output Result ------------------
		
		System.out.printf( "Result: %s; Score: %d\n", cc.ptRes, cc.rscore );

	};
};
