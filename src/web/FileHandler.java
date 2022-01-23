package web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.sun.net.httpserver.HttpExchange;

public class FileHandler implements ContextHandler {

	final Map<String, String> fileNames;
	final ZipFile zipFile;

	public FileHandler(ZipFile zipFile, Map<String, String> idToFile) {
		this.zipFile = zipFile;
		this.fileNames = idToFile;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String[] request = exchange.getRequestURI().getPath().split("/");

		System.out.println(Arrays.toString(request));

		try {
			if (request.length != 3) {
				throw new IllegalArgumentException();
			}

			String zipName = fileNames.get(request[2]);

			if (zipName == null) {
				throw new FileNotFoundException(request[2]);
			}

			ZipEntry zipEntry = zipFile.getEntry(zipName);

			if (zipEntry == null) {
				throw new FileNotFoundException(zipName);
			}

			exchange.sendResponseHeaders(200, Math.max(0, zipEntry.getSize()));

			try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
				try (OutputStream outputStream = exchange.getResponseBody()) {

					byte[] buffer = new byte[1 << 10];
					int len;
					while ((len = inputStream.read(buffer)) > 0) {
						outputStream.write(buffer, 0, len);
					}
				}
			}
		} catch (Exception e) {
			Handler.sendString(exchange, 503, e.getMessage());
			e.printStackTrace();
		}

	}

	@Override
	public String getContext() {
		return "/file/";
	}

}
