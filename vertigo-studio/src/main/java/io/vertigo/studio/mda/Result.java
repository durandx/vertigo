package io.vertigo.studio.mda;

import java.io.File;
import java.io.PrintStream;

/**
 * Résultat de la génération.
 *
 * @author pchretien
 * @version $Id: Result.java,v 1.1 2013/07/11 10:04:05 npiedeloup Exp $
 */
public interface Result {
	/**
	 * Affichage du résultat de la génération dans la console.
	 */
	void displayResultMessage(final PrintStream out);

	/**
	 * Notification de la génération d'un fichier (écrit sur disque).
	 * @param file Fichier généré
	 * @param success Si la génération a réussi 
	 */
	void addFileWritten(final File file, final boolean success);

	/** 
	 * Le fichier est identique
	 * @param file Fichier généré
	 */
	void addIdenticalFile(File file);
}
