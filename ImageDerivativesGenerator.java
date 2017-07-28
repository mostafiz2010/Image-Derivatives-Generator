package ianus.image.resize;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;


public class ImageDerivativesGenerator {
	
	private String inputData;
	private String outputData;
	
	private String resources = "src/main/resources";
	
	
	public ImageDerivativesGenerator(String input, String output) {
		this.inputData = input;
		this.outputData = output;
	}
	
	/**
	 * 
	 * @param String path		the relative path without leading slash
	 */
	public void recurse(String path) throws Exception{
		
		File file = new File(this.inputData);
		String out = this.outputData;
		if(path != null) {
			file = new File(this.inputData + "/" + path);
			out = this.outputData + "/" + path;
		}
		
		if(file.exists() && file.isDirectory()) {
			// replicate the directory in the output folder
			if(!new File(out).exists()) {
				try {
					Files.createDirectories(Paths.get(out));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(path != null) path += "/";
			if(path == null) path = "";
			// iterate over the folder contents...
			for(File _file : file.listFiles()) {
				if(_file.getName().equals(".DS_Store"))
					continue;
				String _path = path + _file.getName();
				if(_file.isFile())
					processFile(_path);
				if(_file.isDirectory())
					recurse(_path);
			}
		}
	}
	
	
	
	private void processFile(String path) throws Exception{
	
	
		generateThumb(path);
		generatePreview(path, 1600);
		generatePreview(path, 800);
		generatePreview(path, 400);
	}
	
	
	
	private void generateThumb(String path) {
		String source = this.inputData + "/" + path;
		String fullpath = path;
		
		String name = path;
		String target;
		if(path.lastIndexOf("/") > 0) {
			name = path.substring(path.lastIndexOf("/") + 1);
			path = path.substring(0, path.lastIndexOf("/") + 1);
			target = this.outputData + "/" + path + "thumb_" + name + ".png";
		}else{
			target = this.outputData + "/thumb_" + name + ".png";
		}
		
		File file = new File(source);
		if(acceptedImageFormat(file)) {
			try {
				//TODO this try is not working
				resizeImage(file, target, 150, 150);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			// create static icons
			this.createIcon(fullpath);
		}
	}
	
	
	
	private void createIcon(String path) {
		String format = this.getFileFormat(path);
		BufferedImage bare = null;
		
		InputStream iconStream = this.getClass().getClassLoader().getResourceAsStream("icons/researchdata-icon.png");
		
		try {
			bare = ImageIO.read(iconStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String name = path;
		String target;
		
		if(path.lastIndexOf("/") > 0) {
			name = path.substring(path.lastIndexOf("/") + 1);
			path = path.substring(0, path.lastIndexOf("/") + 1);
			target = this.outputData + "/" + path + "thumb_" + name + ".png";
		}else{
			target = this.outputData + "/thumb_" + name + ".png";
		}
		
		
		Graphics g = bare.getGraphics();
		g.setFont(new Font("Arial", Font.PLAIN, 19));
	    g.drawString(format, 23, 97);
	    g.dispose();

	    try {
			ImageIO.write(bare, "png", new File(target));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private String getFileFormat(String path) {
		// TODO: do something more advanced than extracting the file extension,
		// eg. reading the file headers, using any file format registry
		String fmt = null;
		if(path.lastIndexOf(".") > 0) {
			fmt = path.substring(path.lastIndexOf('.') + 1);
		}
		return fmt.toLowerCase();
	}
	
	
	
	private void generatePreview(String path, int targetWidth) throws Exception {
		String source = this.inputData + "/" + path;
		String name = path;
		String target;
		if(path.lastIndexOf("/") > 0) {
			name = path.substring(path.lastIndexOf("/") + 1);
			path = path.substring(0, path.lastIndexOf("/") + 1);
			target = this.outputData + "/" + path + "preview_" + Integer.toString(targetWidth) + "_" + name + ".png";
		}else{
			target = this.outputData + "/preview_" + Integer.toString(targetWidth) + "_" + name + ".png";
		}
		
		File file = new File(source);
		if(acceptedImageFormat(file)) {
			resizeImage(file, target, targetWidth, null);
		}else{
			// TODO: create static icons, other visualization handling
		}
	}
	
	
	/**
	 * For basic Java image IO, format will equal one of: 
	 * BMP,bmp,JPEG,jpeg,JPG,jpg,WBMP,wbmp,GIF,gif,PNG,png
	 * Further plugins will be required to read eg. TIFF or JPEG2000.
	 *			
	 * @param File file
	 * @return Boolean
	 */
	private static boolean acceptedImageFormat(File file){
		ImageInputStream stream = null;
		Boolean accepted = true;
		
		try {
			stream = ImageIO.createImageInputStream(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// get all currently registered readers that recognize the image format
		Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
		
		// get the first reader
		if(!iter.hasNext()) {
			accepted = false;
		}
		else{
			ImageReader reader = iter.next();
			try {
				String format = reader.getFormatName();
				if(format == null || format.equals(""))
					accepted = false;
			} catch (IOException e) {
				accepted = false;
				
			}
		}
		
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return accepted;
	}	
	
	
	
	private static void resizeImage(File file, String targetPath, Integer targetWidth, Integer targetHeight ) {
		
		BufferedImage img = null;
		try{
			img = ImageIO.read(file);
		}catch(IOException e){
			e.printStackTrace();
		}
		
		int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		
		BufferedImage resizedImage = img;
		
		int width = img.getWidth();
		int height = img.getHeight();
		
		//keep the original specifications
		Integer _targetHeight = targetHeight;
		Integer _targetWidth = targetWidth;
		
		//if provided size is smaller on at least one dimension, do not scale down any further
		
		Boolean scaling = true;
		if((targetWidth != null && width <= targetWidth) || (targetHeight != null && height <= targetHeight)){
			targetHeight = height;
			targetWidth = width;
			scaling = false;
		}
		
		// if both target dimensions were specified, enable the cropping mechanism to fix the box
		Boolean cropping = false;
		if(_targetWidth != null && _targetHeight != null){
			cropping = true;
			// if supplied img is smaller than specified box, don't crop
			if(_targetWidth > width && _targetHeight > height){
				cropping = false;
			}
		}
		
		// if one targetDimension was ommitted (null),
		// calculate the second target dimension based on the supplied image aspect ration
		if(targetHeight == null){
			double ratio = (double) height / (double) width;
			targetHeight = (int) Math.round(ratio * targetWidth);
		}else if(targetWidth == null){
			double ratio = (double) width / (double) height;
			targetWidth = (int) Math.round(ratio * targetHeight);
		}
		
		if(scaling){
			do{
				if(targetWidth != null && width > targetWidth){
					width /= 2;
					if(width < targetWidth){
						width = targetWidth;
					}
				}
				if(targetHeight != null && height > targetHeight){
					height /= 2;
					if(height < targetHeight){
						height = targetHeight;
					}
				}
				
				BufferedImage tmp = new BufferedImage(width, height, type);
				Graphics2D g2 = tmp.createGraphics();
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.drawImage(resizedImage, 0, 0, width, height, null);
				g2.dispose();
				
				resizedImage = tmp;
			}while((targetWidth != null && width > targetWidth) || (targetHeight != null && height > targetHeight));
		}
		
		// do the cropping
		if(cropping){
			width = resizedImage.getWidth();
			height = resizedImage.getHeight();
			
			int x = 0;
			int y = 0;
			
			if(_targetWidth < width){
				x = (width - _targetWidth) / 2;
			}else{
				_targetWidth = width;
			}
			if(_targetHeight < height){
				y = (height - _targetHeight) / 2;
			}else{
				_targetHeight = height;
			}
			resizedImage = resizedImage.getSubimage(x, y, _targetWidth, _targetHeight);
		}
		
		try {
			ImageIO.write(resizedImage, "PNG", new File(targetPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	
	public static void main(String[] args) throws Exception {
		
		String inputAddress = "/Users/mostafizur/Desktop/Test_Data/ImageResize";
		String outputAddress = "/Users/mostafizur/Desktop/Test_Data/output6";
		
		
		ImageDerivativesGenerator generator = new ImageDerivativesGenerator(inputAddress, outputAddress);
		
		
			System.out.println("Creating thums and previews.....");
			try {
				generator.recurse(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	}
}
