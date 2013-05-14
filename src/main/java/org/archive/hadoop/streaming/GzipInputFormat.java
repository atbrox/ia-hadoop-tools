package org.archive.hadoop.streaming;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.LineRecordReader;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.lib.CombineFileInputFormat;
import org.apache.hadoop.mapred.lib.CombineFileRecordReader;
import org.apache.hadoop.mapred.lib.CombineFileSplit;
import org.archive.util.zip.OpenJDK7GZIPInputStream;

public class GzipInputFormat extends CombineFileInputFormat<LongWritable, Text> {

	@Override
	protected boolean isSplitable(FileSystem fs, Path file) {
		return false;
	}

	@Override
	public RecordReader<LongWritable, Text> getRecordReader(
			InputSplit split, JobConf job, Reporter reporter)
			throws IOException {
		
		 return new CombineFileRecordReader<LongWritable, Text>(job, (CombineFileSplit) split, reporter, (Class)GzipSingleFileRecordReader.class);
	}
	
	public static class GzipSingleFileRecordReader implements RecordReader<LongWritable, Text>
	{
		final LineRecordReader reader;
		
		public GzipSingleFileRecordReader(CombineFileSplit split, Configuration conf, Reporter reporter, Integer index) throws IOException
		{
			final Path file = split.getPath(index);
			
			FileSystem fs = file.getFileSystem(conf);

			FSDataInputStream fileIn = fs.open(file);
						
		    String delimiter = conf.get("textinputformat.record.delimiter");
		    
		    byte[] recordDelimiter = null;
		    
		    if (null != delimiter) {
		    	recordDelimiter = delimiter.getBytes();
		    }
		    
		    InputStream in = new OpenJDK7GZIPInputStream(fileIn);
		    
			reader = new LineRecordReader(in, 0, Long.MAX_VALUE, conf, recordDelimiter);
		}

		@Override
		public boolean next(LongWritable key, Text value) throws IOException {
			return reader.next(key, value);
		}

		@Override
		public LongWritable createKey() {
			return reader.createKey();
		}

		@Override
		public Text createValue() {
			return reader.createValue();
		}

		@Override
		public long getPos() throws IOException {
			return reader.getPos();
		}

		@Override
		public void close() throws IOException {
			reader.close();			
		}

		@Override
		public float getProgress() throws IOException {
			return reader.getProgress();
		}
	}
		
//	public RecordReader<LongWritable, Text> getRecordReader(
//			InputSplit genericSplit, JobConf job, Reporter reporter)
//			throws IOException {
//		
//		CombineFileSplit split = (CombineFileSplit)genericSplit;
//		
//		Vector<InputStream> vec = new Vector<InputStream>();
//		
//		for (Path path : split.getPaths()) {
//			FileSystem fs = path.getFileSystem(job);
//			FSDataInputStream fileIn = fs.open(path);
//			vec.add(fileIn);
//		}
//		
//		InputStream in = new SequenceInputStream(vec.elements());
//		
////		long start = split.getStart();
////		long end = start + split.getLength();
////		final Path file = split.getPath();
////		
//////		compressionCodecs = new CompressionCodecFactory(job);
//////		final CompressionCodec codec = compressionCodecs.getCodec(file);
////
////		// open the file and seek to the start of the split
////		FSDataInputStream fileIn = fs.open(split.getPath());
////		
////		boolean skipFirstLine = false;
////		
////		InputStream in = new OpenJDK7GZIPInputStream(fileIn);
////		
////		if (start != 0) {
////			skipFirstLine = true;
////			--start;
////			fileIn.seek(start);
////		}
//		
//	    String delimiter = job.get("textinputformat.record.delimiter");
//	    byte[] recordDelimiter = null;
//	    if (null != delimiter) {
//	    	recordDelimiter = delimiter.getBytes();
//	    }
//		
////		if (skipFirstLine) { // skip first line and re-establish "start".
////			LineRecordReader.LineReader reader = new LineReader(in, job, recordDelimiter);
////			start += reader.readLine(new Text(), 0,
////					(int) Math.min((long) Integer.MAX_VALUE, end - start));
////		}		
//		
//		return new LineRecordReader(in, 0, Long.MAX_VALUE, job, recordDelimiter);
//	}

}