/*
	JavaZip a simple program for zipping a directory
	Copyright (C) 2021 Davide Sestili

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import it.dsestili.jhashcode.core.DirectoryInfo;
import it.dsestili.jhashcode.core.DirectoryScanner;
import it.dsestili.jhashcode.core.DirectoryScannerRecursive;
import it.dsestili.jhashcode.core.IScanProgressListener;
import it.dsestili.jhashcode.core.ProgressEvent;
import it.dsestili.jhashcode.core.Utils;
import it.dsestili.jhashcode.ui.MainWindow;

public class JavaZip 
{
	protected static final int BUFFER_SIZE = 128 * 1024;
	
	public static void main(String[] args) 
	{
		if(args.length == 1)
		{
			try
			{
				File f = new File(args[0]);
				
				if(!f.exists())
				{
					System.out.println("Does not exist");
					return;
				}
				
				if(!f.isDirectory())
				{
					System.out.println("Is not a dir");
					return;
				}
				
				new JavaZip().zipDir(f);
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
		}
		else
		{
			System.out.println("Usage: directory to zip");
		}
	}
	
	private void zipDir(File dir) throws Throwable
	{
		File[] result = null;
		DirectoryScanner scanner = null;
		
		MainWindow.setExcludeHiddenFiles(false);
		MainWindow.setExcludeSymbolicLinks(false);
		
		scanner = new DirectoryScannerRecursive(dir, true);
		
		scanner.addIScanProgressListener(new IScanProgressListener() {
			public void scanProgressEvent(ProgressEvent event)
			{
				System.out.println(event);
			}
		});
		
		DirectoryInfo di = scanner.getFiles();
		result = di.getFiles();
		long totalSize = di.getTotalSize();
		
		System.out.println("Scanning completed, " + result.length + " files found, " + totalSize + " bytes total size");

		File zipFile = new File(dir + ".zip");
		
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		ZipOutputStream out = null;

		try
		{
			fos = new FileOutputStream(zipFile.getAbsolutePath());
			bos = new BufferedOutputStream(fos);
			out = new ZipOutputStream(bos);

			for(File res : result)
			{
				String relativePath = Utils.getRelativePath(dir.getAbsolutePath(), res.getAbsolutePath());
				System.out.println("Adding: " + res.getAbsolutePath());
				addEntry(relativePath, res.getAbsolutePath(), out);
			}
		}
		finally
		{
			out.close();
		}
		
		System.out.println("Finished: " + zipFile.getAbsolutePath());
	}
	
	protected void addEntry(String fileName, String filePath, ZipOutputStream out) throws Throwable
	{
		byte buf[] = new byte[BUFFER_SIZE];
		
		FileInputStream fis = null;
		BufferedInputStream bis = null;
	
		try
		{
			fis = new FileInputStream(filePath);
			bis = new BufferedInputStream(fis);
			ZipEntry entry = new ZipEntry(fileName);
			out.putNextEntry(entry);
	
			int len;
			while((len = bis.read(buf, 0, buf.length)) != -1)
			{
			   out.write(buf, 0, len);
			}
		}
		finally
		{
			bis.close();
		}
	}
}
