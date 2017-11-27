package sk.fiit.dp.refactor.command;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitCommandHandler {

	final static Logger logger = LoggerFactory.getLogger(GitCommandHandler.class);
	private static GitCommandHandler INSTANCE;

	private Git repo;
	private File localPath;

	private GitCommandHandler() {
	}

	public static GitCommandHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new GitCommandHandler();
		}

		return INSTANCE;
	}

	/**
	 * Vytvori lokalnu kopiu zadaneho git repozitara
	 * 
	 * @param uri
	 * @param name
	 * @param password
	 * @throws IOException
	 * @throws InvalidRemoteException
	 * @throws TransportException
	 * @throws GitAPIException
	 */
	public void cloneRepository(String uri, String name, String password, String id)
			throws IOException, InvalidRemoteException, TransportException, GitAPIException {
		localPath = File.createTempFile(id, "");
		localPath.delete();

		repo = Git.cloneRepository().setURI(uri)
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(name, password)).setDirectory(localPath)
				.call();
		logger.info("Created temp project directory on path " + localPath);
	}

	/**
	 * Vytvori a checkoutne sa na novy branch
	 * 
	 * @param branchName
	 * @throws IOException
	 * @throws RefAlreadyExistsException
	 * @throws RefNotFoundException
	 * @throws InvalidRefNameException
	 * @throws CheckoutConflictException
	 * @throws GitAPIException
	 */
	public void createBranch(String branchName) throws IOException, RefAlreadyExistsException, RefNotFoundException,
			InvalidRefNameException, CheckoutConflictException, GitAPIException {
		repo.checkout().setCreateBranch(true).setName(branchName).call();
		logger.info("Created branch " + branchName);
	}

	/**
	 * Checkout na zadany branch
	 * 
	 * @param branchName
	 * @throws RefAlreadyExistsException
	 * @throws RefNotFoundException
	 * @throws InvalidRefNameException
	 * @throws CheckoutConflictException
	 * @throws GitAPIException
	 */
	public void checkoutBranch(String branchName) throws RefAlreadyExistsException, RefNotFoundException,
			InvalidRefNameException, CheckoutConflictException, GitAPIException {
		repo.checkout().setName(branchName).call();
	}

	/**
	 * Push lokalne vytvorenej branch do git repozitara
	 * 
	 * @param branchName
	 * @param name
	 * @param password
	 * @throws GitAPIException
	 */
	public void pushBranch(String branchName, String name, String password) throws GitAPIException {
		checkoutBranch(branchName);
		repo.add().addFilepattern(".").call();
		repo.commit().setMessage("RafactorFix").call();
		repo.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(name, password)).call();
		logger.info("Pushed branch " + branchName);
	}

	/**
	 * Pristup k cestu k lokalnej git kopii
	 * 
	 * @return
	 */
	public String getRepoDirectory() {
		return localPath.getAbsolutePath();
	}

	/**
	 * Odstranenie localnej kopie
	 * 
	 * @throws IOException
	 */
	public void deleteLocalDirectory() throws IOException {
		repo.close();
		localPath.delete();
	}
}