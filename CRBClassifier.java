
import java.awt.image.BufferedImage;
import java.util.Vector;



public class CRBClassifier
{
	private Vector<CRBDictEntry> dict;
	public int mscore;
	public int mmatch;
	public BufferedImage prevImg;
	public CRBImageComparator ic;
	
	public CRBClassifier()
	{
		dict = new Vector<CRBDictEntry>();
	};
	
	public Vector<CRBDictEntry> getDict()
	{
		return dict;
	};
	
	private int loadEntry( String eqv )
	{
		String fname;
		CRBDictEntry de;
		int rv;
		
		fname = "dict/" + eqv + ".png";
			
		de = new CRBDictEntry();
		de.setEquiv( eqv );
			
		rv = de.loadImage( fname );
		if ( rv < 0 ) {
			if ( "IOQUV07".indexOf( eqv ) == -1 ) {
				// Suppress error message if we see IOQUV07, as these are not in the captcha image
				System.out.printf( "Failed to load dict element %s\n", eqv );
			}
			return -1;
		}
		
		rv = de.procImage();
		if ( rv < 0 ) {
			System.out.printf( "Failed to process dict element %s\n", eqv );
			return -1;
		}
		
		dict.add( de );
		
		return 0;
	};
	
	public int loadDict()
	{
		int i;
		
		char[] t1 = new char[1];
		int rv;
		for ( i = 'A'; i <= 'Z'; i++ ) {
			t1[0] = (char) i;
			rv = loadEntry( new String( t1 ) );
		}
		
		for ( i = '0'; i <= '9'; i++ ) {
			t1[0] = (char) i;
			rv = loadEntry( new String( t1 ) );
		}
		
		return 0;
	};
	
	public String classifyByMinNMatch( BufferedImage inp )
	{
		ic = new CRBImageComparator();
		ic.setImg( inp );
		
		String res = " ";
		mmatch = Integer.MAX_VALUE;
		int rv;
		CRBDictEntry mde = null;
		for ( CRBDictEntry de : dict ) {
			rv = ic.locateMinNMatch( de.getPL() );
			
			if ( rv < mmatch ) {
				mmatch = rv;
				res = de.getEquiv();
				mde = de;
			}
		}
		
		ic.locateMinNMatch( mde.getPL() );
		prevImg = ic.pimg;
		
		return res;
	};
	
	public String classifyByMinScore( BufferedImage inp )
	{
		ic = new CRBImageComparator();
		ic.setImg( inp );
		
		String res = " ";
		mscore = Integer.MAX_VALUE;
		int rv;
		CRBDictEntry mde = null;
		for ( CRBDictEntry de : dict ) {
			rv = ic.locateMaxScore( de.getPL() );
			
			if ( rv < mscore ) {
				mscore = rv;
				res = de.getEquiv();
				mde = de;
			}
		}
		
		ic.locateMaxScore( mde.getPL() );
		prevImg = ic.pimg;
		
		return res;
	};
};
