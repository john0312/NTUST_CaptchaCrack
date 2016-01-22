
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;



public class CRBCaptchaTrainerEntry
{
	public long[][] imgacc;
	public int width;
	public int height;
	public int imgcount;
	
	public String equiv;
	public BufferedImage rimg;
	
	public static final int spacing = 5;
	
	public CRBCaptchaTrainerEntry( int _width, int _height )
	{
		_width += spacing*2;
		_height += spacing*2;
		
		imgacc = new long[_width][_height];
		width = _width;
		height = _height;
		imgcount = 0;

		int i, j;
		
		for ( i = 0; i < width; i++ ) {
			for ( j = 0; j < height; j++ ) {
				imgacc[i][j] = 0;
			}
		}
	};
	
	public void setEquiv( String _equiv ) {
		equiv = _equiv;
	};
	
	public void addImage( BufferedImage img, int xoff, int yoff )
	{
		int v;
		int i, j;
		for ( i = 0; i < img.getWidth(); i++ ) {
			for ( j = 0; j < img.getHeight(); j++ ) {
				v = img.getRGB( i, j );
				v = v & 0x000000FF;
				v = 255 - v;
				imgacc[i+spacing-xoff][j+spacing-yoff] += v;
			}
		}
		imgcount++;
	};
	
	public int formImage()
	{
		if ( imgcount == 0 ) return -1;
		
		int v;
		int i, j;
		rimg = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		for ( i = 0; i < width; i++ ) {
			for ( j = 0; j < height; j++ ) {
				v = (int) Math.round((((double)imgacc[i][j])/((double)imgcount)));
				if ( v > 255 ) v = 255;
				if ( v < 0 ) v = 0;
				v = 255 - v;
				rimg.setRGB( i, j, v | (v<<8) | (v<<16) | 0xFF000000 );
			}
		}
		
		return 0;
	};
	
	public int recreateImage()
	{
		CRBCaptchaCrack cc = new CRBCaptchaCrack();
		cc.setImage( rimg );
		cc.clearIsolatedPixel();
		cc.bimg = CRBCaptchaCrack.convertToAlpha( cc.bimg );
		cc.bimg = CRBCaptchaCrack.nonlinearImgMapping( cc.bimg, 5 );
		cc.partition();
		
		if ( cc.lblk.size() != 1 ) {
			System.out.printf( "Some weird block got %d blocks\n", cc.lblk.size() );
		} else {
			rimg = cc.lblk.get(0);
		}
		
		return 0;
	};
	
	public void writeImage( String alt )
	{
		String fname = "newdict\\" + equiv + alt + ".png";
		
		File fi;
		try {
			fi = new File( fname );
			ImageIO.write( rimg, "PNG", fi );
		} catch ( Exception e ) {
			e.printStackTrace();
			return;
		}
		
		return;
	};
};
