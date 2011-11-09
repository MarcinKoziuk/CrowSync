/*
 * Copyright (c) 2011, Marcin Koziuk <marcin.koziuk@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package nl.koziuk.crowsync.persist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * For loading and saving the game information list.
 * 
 * @author marcin
 */
public class GameFile {

    private final String filename;

    private List<GameInfo> gameList = new LinkedList<GameInfo>();

    /**
     * Creates new Game file loader from filename.
     * 
     * @param filename The filename to be used and/or created.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws SAXException
     */
    public GameFile(String filename) throws IOException, ParserConfigurationException, TransformerException, SAXException {
        this.filename = filename;

        FileInputStream fstream = null;

        try {
            fstream = new FileInputStream(filename);
            parseXML(fstream);
        } catch (FileNotFoundException e) {
            File f;
            f = new File(filename);
            if (!f.exists()) {
                f.createNewFile();

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder parser = factory.newDocumentBuilder();
                Document doc = parser.newDocument();
                PrintStream ps = new PrintStream(f);
                StreamResult result = new StreamResult(ps);
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                DOMSource source = new DOMSource(doc);
                transformer.transform(source, result);
            }
        } finally {
            if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException e) {
                    // om nom nom!
                }
            }
        }
    }

    /**
     * Saves the game list to the game xml file.
     * 
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @throws ParserConfigurationException
     */
    public void save() throws IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        FileOutputStream fstream = null;

        try {
            fstream = new FileOutputStream(filename);
            writeXML(fstream);
        } catch (FileNotFoundException e) {
            File f;
            f = new File(filename);
            if (!f.exists()) {
                f.createNewFile();
                fstream = new FileOutputStream(f);
                writeXML(fstream);
            }
        } finally {
            if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException e) {
                    // om nom nom!
                }
            }
        }
    }

    /**
     * Returns the game info list.
     * 
     * @return The game info list.
     */
    public List<GameInfo> getGameList() {
        return gameList;
    }

    /**
     * Sets the game info list.
     * 
     * @return Sets game info list.
     */
    public void setGameList(List<GameInfo> gameList) {
        this.gameList = gameList;
    }

    /**
     * Writes to the XML file.
     * 
     * @param fstream The output stream to use.
     * @throws ParserConfigurationException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    private void writeXML(FileOutputStream fstream) throws ParserConfigurationException, TransformerFactoryConfigurationError,
            TransformerException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();

        Element gamesElement = doc.createElement("games");
        doc.appendChild(gamesElement);

        for (GameInfo game : gameList) {
            Element gameElement = doc.createElement("game");
            gamesElement.appendChild(gameElement);

            Element nameElement = doc.createElement("name");
            nameElement.appendChild(doc.createTextNode(game.getName()));
            gameElement.appendChild(nameElement);

            Element saveElement = doc.createElement("savepath");
            saveElement.appendChild(doc.createTextNode(game.getSavePath()));
            gameElement.appendChild(saveElement);

            Element exeElement = doc.createElement("exepath");
            exeElement.appendChild(doc.createTextNode(game.getExecutablePath()));
            gameElement.appendChild(exeElement);
        }

        DOMSource source = new DOMSource(doc);
        PrintStream ps = new PrintStream(fstream);
        StreamResult result = new StreamResult(ps);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);

        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);
    }

    /**
     * Parse the XML file.
     * 
     * @param fstream The input stream to use.
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    private void parseXML(FileInputStream fstream) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(fstream);

        Element gamesElement = document.getDocumentElement();

        NodeList gameNodes = gamesElement.getElementsByTagName("game");
        for (int i = 0; i < gameNodes.getLength(); i++) {
            Node gameNode = gameNodes.item(i);

            if (gameNode.getNodeType() == Node.ELEMENT_NODE) {
                Element gameElement = (Element) gameNode;

                NodeList nameElementNodes = gameElement.getElementsByTagName("name");
                NodeList saveElementNodes = gameElement.getElementsByTagName("savepath");
                NodeList exeElementNodes = gameElement.getElementsByTagName("exepath");

                Element nameElement = (Element) nameElementNodes.item(0);
                Element saveElement = (Element) saveElementNodes.item(0);
                Element exeElement = (Element) exeElementNodes.item(0);

                NodeList names = nameElement.getChildNodes();
                NodeList savePaths = saveElement.getChildNodes();
                NodeList exePaths = exeElement.getChildNodes();

                GameInfo gameInfo = new GameInfo();
                gameInfo.setName(names.item(0).getNodeValue());
                gameInfo.setSavePath(savePaths.item(0).getNodeValue());
                gameInfo.setExecutablePath(exePaths.item(0).getNodeValue());

                gameList.add(gameInfo);
            }

        }

    }
}
