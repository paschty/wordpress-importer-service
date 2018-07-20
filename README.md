# WordpressImporterService

A Service which synchronizes Wordpress blog posts with MyCoRe mods objects. The Server Part.

## Development Server

Run `mvn clean jetty:run` to build the project and run a jetty web server.

## Configuration

The configuration should be present in the `.wpimport/` directory of the executing user. There should be a `config.json`.

### Example config.json
```json
{
  "parts": {
    "test": {
      "blog": "https://verfassungsblog.de/",
      "repository": "http://localhost:8291/mir/",
      "parentObject": "mir_mods_00000001",
      "postTempate": "verfassungsblog.template.xml"
    }
  }
}
```

* **blog** - the url to the WordPress instance
* **repository** - the url to the mycore instance
* **parentObject** - the id of the object to which all blogpost will be appended
* **postTemplate** - a file name or path relative to the `.wpimport` folder which contains a mods.xml template. 
Values of the post will be inserted using xpaths ( See also `de.vzg.service.Post2ModsConverter`)

### Database

To check which wordpress post already has a mycore object, all childen of the `parentObject` will be loaded with the 
mycore restapi and all posts will be loaded with the wordpress restapi. Then every post which url is not found in a 
mycore object can be imported.

To load all MyCoRe objects and posts the service needs like 30 minutes. So i decided to save them in a small json based
file in the configuration folder and only update new ones. The files are named: `mycoredb_$hostname.json` and `blogdb_$hostname.json` 

