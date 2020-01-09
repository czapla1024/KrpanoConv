package img;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;

import ar.com.hjg.pngj.*;

public class Krpano {

	public static void main(String[] args) throws IOException {
		//final long time = System.currentTimeMillis();
		Scanner scan = new Scanner(System.in);
        System.out.println("Nazwa pliku Ÿród³owego: ");
		final String fileName = scan.nextLine();
		final File file = new File(fileName);
		if(!file.exists()) System.out.println("Plik nie istnieje!");
		int ist = fileName.lastIndexOf('.');
		if (ist > 0) {
		    String ext = fileName.substring(ist+1).toLowerCase();
		    if (!(ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png"))){
		        System.out.println("Nieobs³ugiwany format!");
				System.out.println("Wciœnij Enter by zamkn¹æ...");
				scan.nextLine();
				scan.close();
				return;
		    }
			int pixelh = 0;
	    	String numbuf = fileName.substring(0, ist) + " (Krpano)." + ext;
	        System.out.println("Nazwa pliku wynikowego (domyœlnie \"" + numbuf + "\"): ");
			String outName = scan.nextLine();
			if(outName.isEmpty()) outName = numbuf;
			
			boolean usePNGJ = true;
			boolean input;
			if(ext.equals("png")) {
				input = false;
				do {
					System.out.println("Czy chcesz wykorzystaæ bibliotekê PNGJ by Leonbloy? (domyœlnie nie)");
					numbuf = scan.nextLine().toLowerCase();
					if (numbuf.equals("y") || numbuf.equals("yes") || numbuf.equals("t") || numbuf.equals("tak") || numbuf.equals("1")) {
						input = true;
						usePNGJ = true;
					}
					if (numbuf.equals("n") || numbuf.equals("no") || numbuf.equals("nie") || numbuf.equals("0")|| numbuf.isEmpty()) {
						input = true;
						usePNGJ = false;
					}
				}
				while (!input);
			}
			
			
			boolean useGC = true;
			input = false;
			do {
				System.out.println("Czy chcesz wymusiæ Garbage Collector? (domyœlnie nie)");
				numbuf = scan.nextLine().toLowerCase();
				if (numbuf.equals("y") || numbuf.equals("yes") || numbuf.equals("tak") || numbuf.equals("1")) {
					input = true;
					useGC = true;
				}
				if (numbuf.equals("n") || numbuf.equals("no") || numbuf.equals("nie") || numbuf.equals("0")|| numbuf.isEmpty()) {
					input = true;
					useGC = false;
				}
			}
			while (!input);
			
		    if(ext.equals("jpg")||ext.equals("jpeg")) {
				int tileHeight = 1, width = 1, index = 0, tiles2 = 0, tiles3 = 0, tiles4 = 0, tiles5 = 0, tiles6 = 0, tileNeg = 0;
				final File out = new File(outName);
				Rectangle rect = null;
				out.createNewFile();
				BufferedImage buf = null;
			    Graphics g = null;
				
				final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
				final ImageWriteParam wrparam = writer.getDefaultWriteParam();
				final FileImageOutputStream output = new FileImageOutputStream(out);
				writer.setOutput(output);
				wrparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				
				final ImageInputStream stream = ImageIO.createImageInputStream(file);
				final Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
				if (readers.hasNext()) {
				    final ImageReader reader = readers.next();
				    final ImageReadParam param = reader.getDefaultReadParam();
				    reader.setInput(stream);
				    while(index<tileHeight) {
					    if(tileHeight==1) {
					    	tileHeight = reader.getHeight(0);
					    	width = reader.getWidth(0);
					    	if(!(tileHeight*6==width || tileHeight*12==width)) {
					    		System.out.println("Obraz ma b³êdne wymiary!");
					    		System.out.println("Wciœnij Enter by zamkn¹æ...");
					    		scan.nextLine();
					    		scan.close();
					    		return;
					    	}
					    	buf= new BufferedImage(width, tileHeight, BufferedImage.TYPE_INT_RGB);
					    	g=buf.getGraphics();
					    	tiles2 = 2*tileHeight;
					    	tiles3 = tileHeight+tiles2;
					    	tiles4 = tileHeight+tiles3;
					    	tiles5 = tileHeight+tiles4;
					    	tiles6 = tileHeight+tiles5;
					    	tileNeg = -tileHeight;
					    	
							do {
								System.out.println("Szerokoœæ bufora w pikselach (domyœlnie wysokoœæ Ÿród³a - " + tileHeight + "): ");
								numbuf = scan.nextLine();
								try{
									pixelh = Integer.parseInt(numbuf);
								}
								catch (Exception e) {
									if(numbuf.isEmpty()){
										pixelh = tileHeight;
									}
								}
							}
							while (pixelh<=0);

					    	rect = new Rectangle(0, 0, tileHeight, pixelh);
					    	
							float jpgComp  = -1f;
							do {
								System.out.println("Wspó³czynnik kompresji w % (domyœlnie 1): ");
								numbuf = scan.nextLine();
								try{
									jpgComp = 1f - Float.parseFloat(numbuf)/100f;
								}
								catch (Exception e) {
									if(numbuf.isEmpty()){
										jpgComp = 0.99f;
									}
								}
							}
							while (jpgComp<0f || jpgComp>1f);

							wrparam.setCompressionQuality(jpgComp);
							
							System.out.println("Przetwarzanie w toku...");
					    }
					    rect.y = index;
					    if(tileHeight - index < rect.height) rect.height = tileHeight - index;
					    for(int i=0; i<width; i+=tiles6) {
					    	rect.x = i;
					    	param.setSourceRegion(rect);
						    g.drawImage(reader.read(0, param), i+tiles3, index, tileNeg, rect.height, null);
						    rect.x+=tileHeight;
					    	param.setSourceRegion(rect);
						    g.drawImage(reader.read(0, param), i+tileHeight, index, tileNeg, rect.height, null);
						    rect.x+=tileHeight;
					    	param.setSourceRegion(rect);
						    g.drawImage(reader.read(0, param), i+tiles4, tileHeight-index, tileHeight, -rect.height, null);
						    rect.x+=tileHeight;
					    	param.setSourceRegion(rect);
						    g.drawImage(reader.read(0, param), i+tiles5, tileHeight-index, tileHeight, -rect.height, null);
						    rect.x+=tileHeight;
					    	param.setSourceRegion(rect);
						    g.drawImage(reader.read(0, param), i+tiles4, index, tileNeg, rect.height, null);
						    rect.x+=tileHeight;
					    	param.setSourceRegion(rect);
						    g.drawImage(reader.read(0, param), i+tiles2, index, tileNeg, rect.height, null);
					    }
						reader.setLocale(null);
						stream.seek(0);
						if(useGC && (index%(250-(250%pixelh))==0 || pixelh>250)) System.gc();
						index+=pixelh;
					}
				}
				g.dispose();
				writer.write(null, new IIOImage(buf, null, null), wrparam);
				System.out.println("Ukoñczono!");
		    }
		    else if(ext.equals("png") && (!usePNGJ)){
		    	int tileHeight = 1, width = 1, index = 0, tiles2 = 0, tiles3 = 0, tiles4 = 0, tiles5 = 0, tiles6 = 0, tileNeg = 0;
				final File out = new File(outName);
				Rectangle rect = null;
				out.createNewFile();
				BufferedImage buf = null;
			    Graphics g = null;
				final ImageInputStream stream = ImageIO.createImageInputStream(file);
				final Iterator<ImageReader> readers= ImageIO.getImageReaders(stream);
				if (readers.hasNext()) {
				    final ImageReader reader = readers.next();
					final ImageReadParam param = reader.getDefaultReadParam();
				    reader.setInput(stream);
					while(index<tileHeight) {
					    if(tileHeight==1) {
					    	tileHeight = reader.getHeight(0);
					    	width = reader.getWidth(0);
					    	if(!(tileHeight*6==width || tileHeight*12==width)) {
					    		System.out.println("Obraz ma b³êdne wymiary!");
					    		System.out.println("Wciœnij Enter by zamkn¹æ...");
					    		scan.nextLine();
					    		scan.close();
					    		return;
					    	}
					    	buf= new BufferedImage(width, tileHeight, BufferedImage.TYPE_INT_RGB);
					    	g=buf.getGraphics();
					    	tiles2 = 2*tileHeight;
					    	tiles3 = tileHeight+tiles2;
					    	tiles4 = tileHeight+tiles3;
					    	tiles5 = tileHeight+tiles4;
					    	tiles6 = tileHeight+tiles5;
					    	tileNeg = -tileHeight;
					    	
							do {
								System.out.println("Szerokoœæ bufora w pikselach (domyœlnie wysokoœæ Ÿród³a - " + tileHeight + "): ");
								numbuf = scan.nextLine();
								try{
									pixelh = Integer.parseInt(numbuf);
								}
								catch (Exception e) {
									if(numbuf.isEmpty()){
										pixelh = tileHeight;
									}
								}
							}
							while (pixelh<=0);

					    	rect = new Rectangle(0, 0, tileHeight, pixelh);
					    	
					    	System.out.println("Przetwarzanie w toku...");
					    }
					    rect.y = index;
					    if(tileHeight - index < rect.height) rect.height = tileHeight - index;
					    for(int i=0; i<width; i+=tiles6) {
					    	rect.x = i;
					    	param.setSourceRegion(rect);
						    g.drawImage(reader.read(0, param), i+tiles3, index, tileNeg, rect.height, null);
						    rect.x+=tileHeight;
					    	param.setSourceRegion(rect);
						    g.drawImage(reader.read(0, param), i+tileHeight, index, tileNeg, rect.height, null);
						    rect.x+=tileHeight;
					    	param.setSourceRegion(rect);
						    g.drawImage(reader.read(0, param), i+tiles4, tileHeight-index, tileHeight, -rect.height, null);
						    rect.x+=tileHeight;
					    	param.setSourceRegion(rect);
						    g.drawImage(reader.read(0, param), i+tiles5, tileHeight-index, tileHeight, -rect.height, null);
						    rect.x+=tileHeight;
					    	param.setSourceRegion(rect);
						    g.drawImage(reader.read(0, param), i+tiles4, index, tileNeg, rect.height, null);
						    rect.x+=tileHeight;
					    	param.setSourceRegion(rect);
						    g.drawImage(reader.read(0, param), i+tiles2, index, tileNeg, rect.height, null);
					    }
					    reader.setLocale(null);
						if(useGC && (index%(250-(250%pixelh))==0 || pixelh>250)) System.gc();
						index+=pixelh;
					}
				}
				g.dispose();
				ImageIO.write(buf, "png", out);
				System.out.println("Ukoñczono!");
		    } else if(ext.equals("png")){
		    	
				do {
					System.out.println("Szerokoœæ bufora w pikselach (domyœlnie 100): ");
					numbuf = scan.nextLine();
					try{
						pixelh = Integer.parseInt(numbuf);
					}
					catch (Exception e) {
						if(numbuf.isEmpty()){
							pixelh = 100;
						}
					}
				}
				while (pixelh<=0);
				
		    	final PngReader pngr = new PngReader(file);
				final PngWriter pngw = new PngWriter(new File(outName), pngr.imgInfo, true);
				final int channels = pngr.imgInfo.channels;
				final int rows = pngr.imgInfo.rows;
		    	if(!(rows*6==pngr.imgInfo.cols || rows*12==pngr.imgInfo.cols)) {
		    		System.out.println("Obraz ma b³êdne wymiary!");
		    		System.out.println("Wciœnij Enter by zamkn¹æ...");
		    		scan.nextLine();
		    		scan.close();
		    		return;
		    	}
		 		final int[][] inv = new int[pixelh][channels*(pngr.imgInfo.cols/3)];
				final int rows1 = rows*channels;
				final int rows2 = 2*rows1;
				final int rows4 = 2*rows2;
				final int rows5 = rows4+rows1;
				final int rows6 = rows5+rows1;
				System.out.println("Przetwarzanie w toku...");
			 	for (int row = 0; row < rows; row++) {
					final IImageLine l1 = pngr.readRow();
					final int[] pixtab = ((ImageLineInt)l1).getScanline();
					if(row%pixelh==0) {
				    	PngReader pngrInv = new PngReader(file);
				    	for(int x = 0; x<inv.length; x++) {
							final IImageLine l2 = pngrInv.readRow(rows-row+x-Math.min(pixelh, rows-row));
							final int[] tempInv = ((ImageLineInt)l2).getScanline();
							for(int i=0; i<pixtab.length; i+=rows6) {
								final int hlimiter = i/3;
								for(int j=0; j<rows2; j++) {
									inv[x][hlimiter+j] = tempInv[i+j+rows2];
								}
							}
				    	}
				    	pngrInv.close();
					}
			    	if(useGC && (row%Math.max(250, pixelh))==0) System.gc();
					final int vlimiter = Math.min(pixelh, rows-row+(row%pixelh))-row%pixelh-1;
					for(int i=0; i<pixtab.length; i+=rows6) {
						final int hlimiter = i/3;
						for(int j=0; j<rows1; j+=channels) {
							final int rev2j = i+rows2-j-channels;
							for(int k = 0; k<channels; k++) {
								final int ijk = i+j+k;
								pixtab[rev2j+k+rows1] = pixtab[ijk];//right->3
								pixtab[ijk] = pixtab[rev2j+k];//left->1
								pixtab[rev2j+k] = pixtab[ijk+rows5];//front->2
								pixtab[rev2j+k+rows2] = pixtab[ijk+rows4];//back->4
								pixtab[ijk+rows4] = inv[vlimiter][hlimiter+j+k];//top->5
								pixtab[ijk+rows5] = inv[vlimiter][hlimiter+rows1+j+k];//bottom->6
							}
						}
					}
					pngw.writeRow(l1);
					}
				 pngr.end();
				 pngw.end();
				 System.out.println("Ukoñczono!");
		    }
		}
		else System.out.println("Plik nie ma rozszerzenia!");
		System.out.println("Wciœnij Enter by zamkn¹æ...");
		scan.nextLine();
		scan.close();
		//System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000000.0);
		//System.out.println((System.currentTimeMillis() - time)/1000.0);
	}
}
