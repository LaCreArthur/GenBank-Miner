projet-bioinfo-2016

## Interface

### Package jxBrowser

#### Console

Permet de lire les messages de Javascript dans la console Java a l'aide d'un listener
Contient des methodes pour ecrire du javascript rapidement
 * JSLog(string s), JSError et JSScript

et des methodes pour ecrire dans la console de l'interface
 * WriteInput, WriteOutput et WriteException

 #### IO
 sert a récupérer les royaumes, on s'en occupe pas pour l'instant, ne pas la modifier

 ##### Tree
 Contient la méthode DrawTree() qui dessine une arborecence a partir d'un dossier racine (ecrit en dur dans la méthode pour l'instant)

 ##### UI

 contient les attributs pour le browser
 browser, view, console, tree, document

 et une methode setProgressBArValue pour changer la valeur de la barre