class Menu{
    String name
    MenuItem[] items
}

class MenuItem{
    String url, title, pageid
}

class Page{
    String pageid, file
}

//=======================================================

def templatecontent = new File('site/pagetemplate.html').text

def menus = [
    new Menu(name:'Menu',items:[
        new MenuItem(title:'Overview',              pageid:'overview'),
        new MenuItem(title:'Contact',               pageid:'contact'),
        new MenuItem(title:'Mission Statement',     pageid:'missionstatement'),
        new MenuItem(title:'Download',              pageid:'download'),
        new MenuItem(title:'Jira',                  url:'https://jira.codehaus.org/browse/PROM'),
        new MenuItem(title:'Wiki',                  url:'http://docs.codehaus.org/display/PROM/Home'),
        new MenuItem(title:'Blog',                  url:'http://pveentjer.wordpress.com'),
        new MenuItem(title:'Test Coverage',         url:'clover/index.html'),
        new MenuItem(title:'License',               pageid:'license')
    ]),
    new Menu(name: 'Javadoc', items:[
        new MenuItem(title:'Prometheus',            url:'javadoc/main/index.html'),
        new MenuItem(title:'Testsupport',           url:'javadoc/testsupport/index.html')
    ]),
    new Menu(name: 'Guides', items:[
        new MenuItem(title:'Repeater',              pageid:'guide-repeater'),
        new MenuItem(title:'BlockingExecutor',      pageid:'guide-blockingexecutor'),
        new MenuItem(title:'References',            pageid:'guide-references'),
        new MenuItem(title:'Misc',                  pageid:'guide-misc'),
        new MenuItem(title:'TestSupport',           pageid:'guide-testsupport')
    ])
]    

//this is redundant information, all pages can be derived from the menu.
def pages = [
    new Page( pageid: 'overview'),
    new Page( pageid: 'contact'),
    new Page( pageid: 'missionstatement'),
    new Page( pageid: 'download'),
    new Page( pageid: 'guide-blockingexecutor'),
    new Page( pageid: 'guide-repeater'),
    new Page( pageid: 'guide-references'),
    new Page( pageid: 'guide-misc'),
    new Page( pageid: 'guide-testsupport'),
    new Page( pageid: 'license')
]

def outputdirectory = 'target/site'

def lastupdate = '30 June, 2007'

//=============== template engine ==================

println('starting')
def outputdirectoryfile = new File(outputdirectory)
if(!outputdirectoryfile.exists()){
    if(!outputdirectoryfile.mkdirs()){
        throw new Exception("file could not be created $outputdirectory")
    }
}



for(page in pages){
    def filename = "${page.pageid}.html"
    def engine   = new groovy.text.GStringTemplateEngine()
    def template = engine.createTemplate(templatecontent)
    def pagecontent = new File("site/$filename").text
    def binding = [ menus: menus,
                    pagecontent: pagecontent,
                    page: page,
                    lastupdate: lastupdate]
    def result = template.make(binding).toString()
    def output = new File("$outputdirectory/$filename")
    println(output)
    output.createNewFile()
    output.write(result)
}

println('finished')

