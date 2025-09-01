// Description: Java 11 XML SAX Loader CLI persisting as PostgreSQL for CFSec.

/*
 *	server.markhome.msscf.CFSec
 *
 *	Copyright (c) 2020-2025 Mark Stephen Sobkow
 *	
 *
 *	Manufactured by MSS Code Factory 2.13
 */

package server.markhome.msscf.cfsec.CFSecSaxPgSqlLdr;

import org.apache.log4j.*;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.msscf.msscf.cflib.CFLib.*;
import server.markhome.msscf.cfsec.CFSec.*;
import server.markhome.msscf.cfsec.CFSecObj.*;
import org.msscf.msscf.cflib.CFLib.*;
import server.markhome.msscf.cfsec.CFSecPgSql.*;
import server.markhome.msscf.cfsec.CFSecSaxLoader.*;

public class CFSecSaxPgSqlLdr
	extends CFSecSaxLdr
{
	private static ICFLibMessageLog log = new CFLibConsoleMessageLog();

	// Constructors

	public CFSecSaxPgSqlLdr() {
		super( log );
	}

	// main() entry point

	public static void main( String args[] ) {
		final String S_ProcName = "CFSecSaxPgSqlLdr.main() ";
		initConsoleLog();
		int numArgs = args.length;
		if( numArgs >= 2 ) {
			String homeDirName = System.getProperty( "HOME" );
			if( homeDirName == null ) {
				homeDirName = System.getProperty( "user.home" );
				if( homeDirName == null ) {
					log.message( S_ProcName + "ERROR: Home directory not set" );
					return;
				}
			}
			File homeDir = new File( homeDirName );
			if( ! homeDir.exists() ) {
				log.message( S_ProcName + "ERROR: Home directory \"" + homeDirName + "\" does not exist" );
				return;
			}
			if( ! homeDir.isDirectory() ) {
				log.message( S_ProcName + "ERROR: Home directory \"" + homeDirName + "\" is not a directory" );
				return;
			}
			CFSecConfigurationFile cFSecConfig = new CFSecConfigurationFile();
			String cFSecConfigFileName = homeDir.getPath() + File.separator + ".cfsecpgsqlrc";
			cFSecConfig.setFileName( cFSecConfigFileName );
			File cFSecConfigFile = new File( cFSecConfigFileName );
			if( ! cFSecConfigFile.exists() ) {
				cFSecConfig.setDbServer( "127.0.0.1" );
				cFSecConfig.setDbPort( 5432 );
				cFSecConfig.setDbDatabase( "CFSec31" );
				cFSecConfig.setDbUserName( "postgres" );
				cFSecConfig.setDbPassword( "edit-me-please" );
				cFSecConfig.save();
				log.message( S_ProcName + "INFO: Created configuration file " + cFSecConfigFileName + ", please edit configuration and restart." );
				return;
			}
			if( ! cFSecConfigFile.isFile() ) {
				log.message( S_ProcName + "ERROR: Proposed configuration file " + cFSecConfigFileName + " is not a file." );
				return;
			}
			if( ! cFSecConfigFile.canRead() ) {
				log.message( S_ProcName + "ERROR: Permission denied attempting to read configuration file " + cFSecConfigFileName );
				return;
			}
			cFSecConfig.load();
			boolean fastExit = false;
			CFSecClientConfigurationFile cFDbTestClientConfig = new CFSecClientConfigurationFile();
			String cFDbTestClientConfigFileName = homeDir.getPath() + File.separator + ".cfdbtestclientrc";
			cFDbTestClientConfig.setFileName( cFDbTestClientConfigFileName );
			File cFDbTestClientConfigFile = new File( cFDbTestClientConfigFileName );
			if( ! cFDbTestClientConfigFile.exists() ) {
				String cFDbTestKeyStoreFileName = homeDir.getPath() + File.separator + ".msscfjceks";
				cFDbTestClientConfig.setKeyStore( cFDbTestKeyStoreFileName );
				InetAddress localHost;
				try {
					localHost = InetAddress.getLocalHost();
				}
				catch( UnknownHostException e ) {
					localHost = null;
				}
				if( localHost == null ) {
					log.message( S_ProcName + "ERROR: LocalHost is null" );
					return;
				}
				String hostName = localHost.getHostName();
				if( ( hostName == null ) || ( hostName.length() <= 0 ) ) {
					log.message( "ERROR: LocalHost.HostName is null or empty" );
					return;
				}
				String userName = System.getProperty( "user.name" );
				if( ( userName == null ) || ( userName.length() <= 0 ) ) {
					log.message( "ERROR: user.name is null or empty" );
					return;
				}
				String deviceName = hostName.replaceAll( "[^\\w]", "_" ).toLowerCase()
					+ "-"
					+ userName.replaceAll( "[^\\w]", "_" ).toLowerCase();
				cFDbTestClientConfig.setDeviceName( deviceName );
				cFDbTestClientConfig.save();
				log.message( S_ProcName + "INFO: Created CFSec client configuration file " + cFDbTestClientConfigFileName );
				fastExit = true;
			}
			if( ! cFDbTestClientConfigFile.isFile() ) {
				log.message( S_ProcName + "ERROR: Proposed client configuration file " + cFDbTestClientConfigFileName + " is not a file." );
				fastExit = true;
			}
			if( ! cFDbTestClientConfigFile.canRead() ) {
				log.message( S_ProcName + "ERROR: Permission denied attempting to read client configuration file " + cFDbTestClientConfigFileName );
				fastExit = true;
			}
			cFDbTestClientConfig.load();

			if( fastExit ) {
				return;
			}

			// Configure logging
			Properties sysProps = System.getProperties();
			sysProps.setProperty( "log4j.rootCategory", "WARN" );
			sysProps.setProperty( "org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Log4JLogger" );

			Logger httpLogger = Logger.getLogger( CFSecSaxPgSqlLdr.class );
			httpLogger.setLevel( Level.WARN );

			ICFSecSchema cFSecSchema = new CFSecPgSqlSchema();
			cFSecSchema.setConfigurationFile( cFSecConfig );
			ICFSecSchemaObj cFSecSchemaObj = new CFSecSchemaObj();
			cFSecSchemaObj.setBackingStore( cFSecSchema );
			CFSecSaxLdr cli = new CFSecSaxPgSqlLdr();
			CFSecSaxLoader loader = cli.getSaxLoader();
			loader.setSchemaObj( cFSecSchemaObj );
			cFSecSchema.connect();
			String url = args[1];
			if( numArgs >= 5 ) {
				cli.setClusterName( args[2] );
				cli.setTenantName( args[3] );
				cli.setSecUserName( args[4] );
			}
			else {
				cli.setClusterName( "default" );
				cli.setTenantName( "system" );
				cli.setSecUserName( "system" );
			}
			loader.setUseCluster( cli.getClusterObj() );
			loader.setUseTenant( cli.getTenantObj() );
			try {
				cFSecSchema.beginTransaction();
				cFSecSchemaObj.setSecCluster( cli.getClusterObj() );
				cFSecSchemaObj.setSecTenant( cli.getTenantObj() );
				cFSecSchemaObj.setSecUser( cli.getSecUserObj() );
				cFSecSchemaObj.setSecSession( cli.getSecSessionObj() );
				CFSecAuthorization auth = new CFSecAuthorization();
				auth.setSecCluster( cFSecSchemaObj.getSecCluster() );
				auth.setSecTenant( cFSecSchemaObj.getSecTenant() );
				auth.setSecSession( cFSecSchemaObj.getSecSession() );
				cFSecSchemaObj.setAuthorization( auth );
				applyLoaderOptions( loader, args[0] );
				if( numArgs >= 5 ) {
					cli.evaluateRemainingArgs( args, 5 );
				}
				else {
					cli.evaluateRemainingArgs( args, 2 );
				}
				loader.parseFile( url );
				cFSecSchema.commit();
				cFSecSchema.disconnect( true );
			}
			catch( Exception e ) {
				log.message( S_ProcName + "EXCEPTION: Could not parse XML file \"" + url + "\": " + e.getMessage() );
				e.printStackTrace( System.out );
			}
			catch( Error e ) {
				log.message( S_ProcName + "ERROR: Could not parse XML file \"" + url + "\": " + e.getMessage() );
				e.printStackTrace( System.out );
			}
			finally {
				if( cFSecSchema.isConnected() ) {
					cFSecSchema.rollback();
					cFSecSchema.disconnect( false );
				}
			}
		}
		else {
			log.message( S_ProcName + "ERROR: Expected at least two argument specifying the loader options and the name of the XML file to parse.  The first argument may be empty." );
		}
	}

	// Initialize the console log

	protected static void initConsoleLog() {
//		Layout layout = new PatternLayout(
//				"%d{ISO8601}"		// Start with a timestamp
//			+	" %-5p"				// Include the severity
//			+	" %C.%M"			// pkg.class.method()
//			+	" %F[%L]"			// File[lineNumber]
//			+	": %m\n" );			// Message text
//		BasicConfigurator.configure( new ConsoleAppender( layout, "System.out" ) );
	}

	// Evaluate remaining arguments

	public void evaluateRemainingArgs( String[] args, int consumed ) {
		// There are no extra arguments for the PostgreSQL database instance
		if( args.length > consumed ) {
			log.message( "CFSecSaxPgSqlLdr.evaluateRemainingArgs() WARNING No extra arguments are expected for a RAM database instance, but "
				+ Integer.toString( args.length - consumed )
				+ " extra arguments were specified.  Extra arguments ignored." );
		}
	}

}
