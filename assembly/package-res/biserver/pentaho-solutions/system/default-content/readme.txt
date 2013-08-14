This folder is where default content is loaded from.  Zip files containing content (reports, ktr, etc)
are loaded into the repository on server startup.  

   - The folder structure in the zip files are loaded in at the root (/).

   - On a successful import the zip file will be renamed with the timestamp of when the
     import finished appended to the file name.  The next start of the server will not
     import that file as it is no longer ending with .zip.

   - If the zip(s) were already imported the server will still start.  Exceptions will
     be reported to the console.
