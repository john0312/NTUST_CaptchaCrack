
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Vector;



public class CRBImageComparator
{
	public BufferedImage img;
	public BufferedImage pimg;
	public int mcnt;
	public int ncnt;
	public int rcnt;
	public int mpcnt; // Maximum possible match
	public int mpscr; // Maximum possible score
	
	public int mmatch;
	public int mscr;
	
	public int box;
	public int boy;
	
	public CRBImageComparator()
	{
	};
	
	public void setImg( BufferedImage _img )
	{
		img = _img;
	};
	
	public static BufferedImage cloneImage( BufferedImage _img )
	{
		BufferedImage nimg = new BufferedImage( _img.getWidth(), _img.getHeight(), BufferedImage.TYPE_INT_ARGB );
		
		int i, j;
		for ( i = 0; i < _img.getHeight(); i++ ) {
			for ( j = 0; j < _img.getWidth(); j++ ) {
				nimg.setRGB( j+nimg.getMinX(), i+nimg.getMinY(), _img.getRGB( j+_img.getMinX(), i+_img.getMinY() ) );
			}
		}
		
		return nimg;
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
	
	public int analysePatternPos( Vector<Point> blk, int ox, int oy )
	{
		BufferedImage nimg = cloneImage( img );
		
		int _mcount = 0; // Matches
		int _ncount = 0; // Doesn't not matches
		
		for ( Point p : blk ) {
			if ( ( ( cappedGetRGB( nimg, p.x+nimg.getMinX()+ox, p.y+nimg.getMinY()+oy ) ) & 0x000000FF ) == 0 ) {
				// Black tile, match
				_mcount++;
				nimg.setRGB( p.x+nimg.getMinX()+ox, p.y+nimg.getMinY()+oy, 0xFFFFFFFF );
			} else {
				_ncount++;
			}
		}
		
		int _rcount = 0; // Remaining count
		int i, j;
		for ( i = 0; i < nimg.getHeight(); i++ ) {
			for ( j = 0; j < nimg.getWidth(); j++ ) {
				if ( ( ( cappedGetRGB( nimg, j+nimg.getMinX(), i+nimg.getMinY() ) ) & 0x000000FF ) == 0 ) {
					_rcount++;
				}
			}
		}
		
		mcnt = _mcount;
		ncnt = _ncount;
		rcnt = _rcount;
		
		mpcnt = mcnt+ncnt;
		
		pimg = nimg;
		return 0;
	};
	
	public int locateMinNMatch( Vector<Point> blk )
	{
		box = boy = 0;
		int max = Integer.MIN_VALUE;
		int i, j;
		for ( i = -3; i < 4; i++ ) {
			for ( j = -3; j < 4; j++ ) {
				analysePatternPos( blk, j, i );
				if ( mcnt > max ) {
					max = mcnt;
					box = j;
					boy = i;
				}
			}
		}
		
		analysePatternPos( blk, box, boy );
		
		mmatch = max;
		
		return (mpcnt-max);
	};
	
	public int getScore()
	{
		int res;
		
		res = 0;
		res += mcnt*2;
		res -= ncnt*2;
		res -= rcnt;
		
		mpscr = mpcnt*2;
		
		return res;
	};
	
	public int locateMaxScore( Vector<Point> blk )
	{
		
		box = boy = 0;
		int max = Integer.MIN_VALUE;
		int i, j;
		int scr;
		for ( i = -3; i < 4; i++ ) {
			for ( j = -3; j < 4; j++ ) {
				analysePatternPos( blk, j, i );
				scr = getScore();
				if ( scr > max ) {
					max = scr;
					box = j;
					boy = i;
				}
			}
		}
		
		analysePatternPos( blk, box, boy );
		
		mscr = max;
		
		return (mpscr-max);
	};
};
