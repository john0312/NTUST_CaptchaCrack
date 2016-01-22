
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import javax.imageio.ImageIO;



public class CRBDictEntry
{
	private Vector<Point> plist;
	private String equiv; // Equivalent character
	private BufferedImage img; // The original image
	
	public CRBDictEntry()
	{
	};
	
	public void setEquiv( String _equiv ) {
		equiv = _equiv;
	};
	
	public String getEquiv()
	{
		return equiv;
	};
	
	public BufferedImage getImage()
	{
		return img;
	};
	
	public static BufferedImage loadBufferedImageFromFile( String fname ) throws IOException
	{
		File fi;
		BufferedImage img;
		try {
			fi = new File( fname );
			img = ImageIO.read( fi );
		} catch ( NullPointerException e ) {
			throw new IOException();
		} catch ( IOException e ) {
			throw e;
		}
		
		return img;
	};
	
	public int loadImage( String fname )
	{
		try {
			img = loadBufferedImageFromFile( fname );
		} catch ( IOException e ) {
			return -1;
		}
		
		return 0;
	};
	
	public static Vector<Point> imgToPList( BufferedImage bimg )
	{
		Vector<Point> res = new Vector<Point>();
		
		int i, j;
		for ( i = 0; i < bimg.getHeight(); i++ ) {
			for ( j = 0; j < bimg.getWidth(); j++ ) {
				if ( ( bimg.getRGB( j+bimg.getMinX(), i+bimg.getMinY() ) & 0x000000FF ) == 0 ) {
					res.add( new Point( j, i ) );
				}
			}
		}
		
		return res;
	};
	
	public int procImage()
	{
		plist = imgToPList( img );
		return 0;
	};
	
	public Vector<Point> getPL()
	{
		return plist;
	};
};
