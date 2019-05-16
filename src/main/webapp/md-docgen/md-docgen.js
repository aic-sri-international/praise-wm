// Creates HTML from this project's markdown documentation and then
// launches it the default browser.
import fs from 'fs-extra';
import path from 'path';
import MarkdownIt from 'markdown-it';
import markdownItGithubToc from 'markdown-it-github-toc';

const md = new MarkdownIt();
md.use(markdownItGithubToc, { toc: false, anchorLink: false });
md.set({ html: true, breaks: true, typographer: true });

const opn = require('opn');

try {
  process.chdir('..');
} catch (err) {
  console.log(`chdir: ${err}`);
}

const docsSrcDir = '../../../docs';
const readmeSrc = '../../../README.md';
const docsDestDir = '../../../build/md-docs';
const stylesheet = path.join(__dirname, 'md-docgen/md-docgen.css');

const mdCssLink = `<link rel="stylesheet" href="${stylesheet}">\n`;
const mdToHtml = (inputFile, outputDir) => {
  const htmlStart = `${'<!DOCTYPE html>\n'
      + '<html>\n'
      + '  <head>\n'
      + '    <meta charset="utf-8">\n'
      + '    <title>README.md</title>\n'}${
    mdCssLink}\n`
      + '<link rel="stylesheet"\n'
      + '      href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.12.0/styles/default.min.css">\n'
      + '<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.12.0/highlight.min.js"></script>'
      + '<script>hljs.configure({ languages: ["none"] });hljs.initHighlightingOnLoad();</script>'
      + '  </head><body style="margin-left:20px;margin-right:20px;margin-bottom:20px;font-size: 1.1em;">';

  const htmlEnd = '</body>'
      + '</html>';

  const cnv = html => html.replace(/\.md">/g, '.html">')
    .replace(/ href="\.\.\//g, ' href="../../../')
    .replace(/ href="\.\//g, ' href="../../');

  const contents = fs.readFileSync(inputFile, 'utf8');
  const html = htmlStart + cnv(md.render(contents)) + htmlEnd;
  const baseName = path.basename(inputFile, '.md');
  fs.writeFileSync(path.join(outputDir, `${baseName}.html`), html, 'utf8');
};

const createDocs = async () => {
  const filterGen = () => (src, dest) => {
    if (fs.statSync(src).isDirectory()
        || src.endsWith('.png') || src.endsWith('.jpg')) {
      return true;
    }

    if (src.endsWith('.md')) {
      mdToHtml(src, path.dirname(dest));
    }

    return false;
  };

  try {
    await fs.emptyDir(docsDestDir);
    mdToHtml(readmeSrc, docsDestDir);
    await fs.copy(
      docsSrcDir, path.join(docsDestDir, 'docs'),
      { filter: filterGen() },
    );
    const url = `file://${path.resolve(path.join(docsDestDir, 'README.html'))}`;
    console.log(`HTML documentation created: ${url}`);
    opn(url);
    process.exit();
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error(err);
    process.exit();
  }
};

createDocs();
