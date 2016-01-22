
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Vector;
import javax.imageio.ImageIO;



public class CRBCaptchaCrack
{
	public BufferedImage bimg;
	public Vector<BufferedImage> lblk;
	public String ptRes; // Plain Text Result
	public int threshold;
	public int rscore;
	public static final int initial_threshold = 25;
	
	public CRBCaptchaCrack()
	{
		lblk = new Vector<BufferedImage>();
		threshold = initial_threshold;
		ptRes = "";
	};
	
	public void setThreshold( int _t )
	{
		threshold = _t;
	};
	
	public int decodeImage( byte[] imgdata )
	{
		ByteArrayInputStream bais = new ByteArrayInputStream( imgdata );
		try {
			bimg = ImageIO.read( bais );
		} catch ( IOException e ) {
			return -1;
		}
		
		return 0;
	};
	
	public void setImage( BufferedImage _img )
	{
		bimg = _img;
	};
	
	static public int cappedGetRGB( BufferedImage img, int x, int y )
	{
		if ( x < img.getMinX() || x >= (img.getMinX()+img.getWidth()) ) {
			return 0xFFFFFFFF;
		}
		
		if ( y < img.getMinY() || y >= (img.getMinY()+img.getHeight()) ) {
			return 0xFFFFFFFF;
		}
		
		return img.getRGB( x, y );
	};
	
	static public int countNonWhite( BufferedImage img, int x, int y )
	{
		int count = 0;
		
		if ( ( cappedGetRGB( img, x+1, y+0 ) & 0x00FFFFFF ) == 0x00FFFFFF ) count+=2;
		if ( ( cappedGetRGB( img, x+0, y+1 ) & 0x00FFFFFF ) == 0x00FFFFFF ) count+=2;
		if ( ( cappedGetRGB( img, x-1, y+0 ) & 0x00FFFFFF ) == 0x00FFFFFF ) count+=2;
		if ( ( cappedGetRGB( img, x+0, y-1 ) & 0x00FFFFFF ) == 0x00FFFFFF ) count+=2;
		
		if ( ( cappedGetRGB( img, x+1, y+1 ) & 0x00FFFFFF ) == 0x00FFFFFF ) count++;
		if ( ( cappedGetRGB( img, x-1, y+1 ) & 0x00FFFFFF ) == 0x00FFFFFF ) count++;
		if ( ( cappedGetRGB( img, x+1, y-1 ) & 0x00FFFFFF ) == 0x00FFFFFF ) count++;
		if ( ( cappedGetRGB( img, x-1, y-1 ) & 0x00FFFFFF ) == 0x00FFFFFF ) count++;
		
		return count;
	};
	
	public int clearIsolatedPixel()
	{
		BufferedImage nimg = new BufferedImage( bimg.getWidth(), bimg.getHeight(), BufferedImage.TYPE_INT_ARGB );
		int i, j;
		for ( i = 0; i < bimg.getHeight(); i++ ) {
			for ( j = 0; j < bimg.getWidth(); j++ ) {
				if ( countNonWhite( bimg, bimg.getMinX()+j, bimg.getMinY()+i ) >= 10 ) {
					// cover by non white tile
					nimg.setRGB( nimg.getMinX()+j, nimg.getMinY()+i, 0xFFFFFFFF );
				} else {
					nimg.setRGB( nimg.getMinX()+j, nimg.getMinY()+i, bimg.getRGB( bimg.getMinX()+j, bimg.getMinY()+i ) );
				}
			}
		}
		
		bimg = nimg;

		return 0;
	};
	
	public static BufferedImage convertToAlpha( BufferedImage img )
	{
		BufferedImage nimg = new BufferedImage( img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB );
		int i, j;
		int R, G, B;
		int Avg;
		int p;
		for ( i = 0; i < img.getHeight(); i++ ) {
			for ( j = 0; j < img.getWidth(); j++ ) {
				p = img.getRGB( img.getMinX()+j, img.getMinY()+i );
				R = p&0x000000FF;
				G = (p&0x0000FF00)>>8;
				B = (p&0x00FF0000)>>16;
				Avg = (R+G+B)/3;
				if ( Avg < 0 ) Avg = 0;
				if ( Avg > 0x000000FF ) Avg = 0x000000FF;
				Avg = Avg & 0x000000FF;
				p = 0xFF000000 | Avg | Avg<<8 | Avg<<16;
				nimg.setRGB( nimg.getMinX()+j, nimg.getMinY()+i, p );
			}
		}
		
		return nimg;
	};
	
	public static int getR( BufferedImage img, int x, int y )
	{
		int p;
		p = img.getRGB( x,y );
		return (p&0x000000FF);
	};
	
	public static BufferedImage nonlinearImgMapping( BufferedImage img )
	{
		return nonlinearImgMapping( img, 253 );
	};
	
	public static BufferedImage nonlinearImgMapping( BufferedImage img, int nthres )
	{
		BufferedImage nimg = new BufferedImage( img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB );
		int i, j;
		int R;
		int Avg;
		int p;
		for ( i = 0; i < img.getHeight(); i++ ) {
			for ( j = 0; j < img.getWidth(); j++ ) {
				R = getR( img, img.getMinX()+j, img.getMinY()+i );
				if ( R > nthres ) {
					R = 255;
				} else {
					R = 0;
				}
				nimg.setRGB( nimg.getMinX()+j, nimg.getMinY()+i, 0xFF000000|R|R<<8|R<<16 );
			}
		}

		return nimg;
	};
	
	public static BufferedImage renderBlock( Collection<Point> blk, int minX, int maxX, int minY, int maxY )
	{
		BufferedImage nimg = new BufferedImage( (maxX-minX+1), (maxY-minY+1), BufferedImage.TYPE_INT_ARGB );

		int i, j;
		for ( i = 0; i < nimg.getHeight(); i++ ) {
			for ( j = 0; j < nimg.getWidth(); j++ ) {
				nimg.setRGB( j+nimg.getMinX(), i+nimg.getMinY(), 0xFFFFFFFF );
			}
		}
		
		for ( Point p : blk ) {
			nimg.setRGB( p.x-minX+nimg.getMinX(), p.y-minY+nimg.getMinY(), 0xFF000000 );
		}
		
		return nimg;
	};
	
	public int BFSonPoint( int sx, int sy )
	{
		Deque<Point> q = new LinkedList<Point>();
		Deque<Point> res = new ArrayDeque<Point>();
		
		q.addLast( new Point( sx, sy ) );
		bimg.setRGB( sx, sy, 0xFFFFFFFF );
		
		Point p;
		int x, y;
		int minX, maxX, minY, maxY;
		minX = minY = Integer.MAX_VALUE;
		maxX = maxY = Integer.MIN_VALUE;
		
		while ( true ) {
			if ( q.size() == 0 ) {
				// We're done
				break;
			}
			p = q.removeFirst();
			
			x = p.x;
			y = p.y;
			
			bimg.setRGB( x, y, 0xFFFFFFFF );
			
			if ( x-1 >= bimg.getMinX() ) {
				if ( ( bimg.getRGB( x-1, y )&0x000000FF ) == 0 ) {
					// Black tile, we can add it
					q.addLast( new Point( x-1, y ) );
					bimg.setRGB( x-1, y, 0xFFFFFFFF );
				}
			}
			
			if ( x+1 < (bimg.getMinX()+bimg.getWidth()) ) {
				if ( ( bimg.getRGB( x+1, y )&0x000000FF ) == 0 ) {
					// Black tile, we can add it
					q.addLast( new Point( x+1, y ) );
					bimg.setRGB( x+1, y, 0xFFFFFFFF );
				}
			}
			
			if ( y-1 >= bimg.getMinY() ) {
				if ( ( bimg.getRGB( x, y-1 )&0x000000FF ) == 0 ) {
					// Black tile, we can add it
					q.addLast( new Point( x, y-1 ) );
					bimg.setRGB( x, y-1, 0xFFFFFFFF );
				}
			}
			
			if ( y+1 < (bimg.getMinY()+bimg.getHeight()) ) {
				if ( ( bimg.getRGB( x, y+1 )&0x000000FF ) == 0 ) {
					// Black tile, we can add it
					q.addLast( new Point( x, y+1 ) );
					bimg.setRGB( x, y+1, 0xFFFFFFFF );
				}
			}
			
			if ( x < minX ) minX = x;
			if ( x > maxX ) maxX = x;
			if ( y < minY ) minY = y;
			if ( y > maxY ) maxY = y;
			
			res.addLast( new Point( x, y ) );
		}
		
		if ( res.size() < 5 ) {
			// Heck it
			//System.out.printf( "Detected block with size %d\n", res.size() );
		} else {
			lblk.add( renderBlock( res, minX, maxX, minY, maxY ) );
		}
		
		return 0;
	};

	public int normalize()
	{
		clearIsolatedPixel();
		bimg = convertToAlpha( bimg );
		bimg = nonlinearImgMapping( bimg );
		return 0;
	};
	
	public int partition()
	{
		int i, j;
		
		for ( j = 0; j < bimg.getWidth(); j++ ) {
			for ( i = 0; i < bimg.getHeight(); i++ ) {
				if ( ( bimg.getRGB( j+bimg.getMinX(), i+bimg.getMinY() ) & 0x000000FF ) == 0 ) {
					// Black tile, init BFS
					BFSonPoint( j+bimg.getMinX(), i+bimg.getMinY() );
				}
			}
		}
		
		return 0;
	};
	
	static private CRBClassifier ccl;
	static public void initClassifier()
	{
		ccl = new CRBClassifier();
		ccl.loadDict();
	};
	
	public int classify()
	{
		int i;
		int rv;
		String res = "";
		String srv;

		rscore = 0;
		i = 0;
		for ( BufferedImage img : lblk ) {
			i++;
			srv = ccl.classifyByMinScore( img );
			rv = ccl.mscore;
			if ( rv > threshold ) {
				// That's some problem
				rscore += rv;
				srv = ccl.classifyByMinNMatch( img );
				res += srv;
				
				CRBCaptchaCrack cc = new CRBCaptchaCrack();
				cc.setImage( ccl.prevImg );
				cc.setThreshold( initial_threshold*2 );
				cc.normalize();
				cc.partition();
				cc.classify();
				res += cc.ptRes;
				rscore += cc.rscore;
			} else {
				res += srv;
				rscore += rv;
			}
		}
		
		ptRes = res;
		
		return 0;
	};
};
