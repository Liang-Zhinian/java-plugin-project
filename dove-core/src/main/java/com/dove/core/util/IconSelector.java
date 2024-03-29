package com.dove.core.util;

import org.apache.commons.io.FilenameUtils;

/**
 * utility class to select an icon based on a file extension
 * 
 * @author Sebastian Stein
 */
public class IconSelector {

	/** returns the icon by parsing the provided file extension */
	public static String selectIcon(String ext) {
		return selectIcon(ext, false);
	}

	/**
	 * Returns the icon by parsing the provided file extension
	 * 
	 * @param shortcut If the icon displays a shortcut
	 */
	public static String selectIcon(String ext, boolean shortcut) {
		String icon = "";
		if (ext != null)
			ext = ext.toLowerCase();

		if (ext == null || ext.equalsIgnoreCase(""))
			icon = "generic.png";
		else if (ext.equals("pdf"))
			icon = "pdf.png";
		else if (ext.equals("txt") || ext.equals("properties") || ext.equals("log") || ext.equals("csv"))
			icon = "text.png";
		else if (ext.equals("doc") || ext.equals("docm") || ext.equals("docx") || ext.equals("docxm") || ext.equals("odt")
				|| ext.equals("rtf") || ext.equals("ott") || ext.equals("sxw") || ext.equals("wpd")
				|| ext.equals("kwd") || ext.equals("dot"))
			icon = "word.png";
		else if (ext.equals("xls") || ext.equals("xlsm") || ext.equals("xlsb") || ext.equals("xlsx") || ext.equals("xlsxm")
				|| ext.equals("ods") || ext.equals("xlt") || ext.equals("ots") || ext.equals("sxc")
				|| ext.equals("dbf") || ext.equals("ksp") || ext.equals("odb"))
			icon = "excel.png";
		else if (ext.equals("ppt") || ext.equals("pptm") || ext.equals("pptx") || ext.equals("pptxm") || ext.equals("odp") || ext.equals("pps") || ext.equals("otp")
				|| ext.equals("pot") || ext.equals("sxi") || ext.equals("kpr"))
			icon = "powerpoint.png";
		else if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("gif") || ext.equals("png") || ext.equals("bmp")
				|| ext.equals("tif") || ext.equals("tiff") || ext.equals("psd"))
			icon = "picture.png";
		else if (ext.equals("htm") || ext.equals("html") || ext.equals("xml") || ext.equals("xhtml"))
			icon = "html.png";
		else if (ext.equals("eml") || ext.equals("msg") || ext.equals("mail"))
			icon = "email.png";
		else if (ext.equals("zip") || ext.equals("rar") || ext.equals("gz") || ext.equals("tar") || ext.equals("jar")
				|| ext.equals("7z"))
			icon = "zip.png";
		else if (ext.equals("p7m") || ext.equals("m7m"))
			icon = "p7m.png";
		else if (ext.equals("dwg") || ext.equals("dxf") || ext.equals("dwt"))
			icon = "dwg.png";
		else if (ext.equals("avi") || ext.equals("mpg") || ext.equals("mp4") || ext.equals("mov") || ext.equals("divx")
				|| ext.equals("wmv") || ext.equals("flv") || ext.equals("mkv") || ext.equals("mpeg") || ext.equals("m2v") || ext.equals("m2ts"))
			icon = "film.png";
		else if (ext.equals("mp3") || ext.equals("m4p") || ext.equals("m4a") || ext.equals("wav") || ext.equals("wma")
				|| ext.equals("cda") || ext.equals("wave"))
			icon = "music.png";
		else if (ext.equals("vsd") || ext.equals("vsdx") || ext.equals("pub") || ext.equals("pubx") || ext.equals("ai"))
			icon = "vector.png";
		else
			icon = "generic.png";

		if (shortcut)
			icon = FilenameUtils.getBaseName(icon) + "-sc." + FilenameUtils.getExtension(icon);

		return icon;
	}
}