- sprint 1 : Redirection directe vers la ressource demandee si elle existe sinon passer par FrontSerlvet et afficher avec PrintWritter l url demande

- sprint 2 : annote les classes controllers et ls methodes a l interieur avec l url 
- sprint 2 bis : scann les fichiers depuis un package racine donner dans un web.xml puis instancie dans un servletcontext dans un hashmap les urls leurs controllers et leurs methodes et dis si une ressource est trouve ou non : erreur si on annote deux fois meme url, erreur si on annote une methode statique, erreur si on annote la methode sans annoter la classe controller, erreur si la methode annote n est pas public,

- sprint 3 : liste dans la page par url la classe et la methode
- sprint 3 bis : /{id} est supporte par l application

- sprint 4 :  cree une classe modelView et dedans mettre un atttribut string view qui recoit le nom de la vue a arriver : si le retour de la methode annote dans le controller est de type ModelView alors on recupere la vue et on redirge la bas si String on affiche le contenu de la String

- sprint 5 : on ajoute un attibut hashmap dans modelView que l on peut remplir et envoyer dans la vue

- sprint 6 : on recupere les donnees depuis les vues a partir des paramtres des fonctions annotees par le controller dont le nom des paramtres sont le nom des keys des request.getParameter
- sprint 6 bis : on ajoute une annotation @RequestParam("key") pour specifier le nom du parametre a recuperer depuis la requete et on compare le key dans l annotation avec le nom des parametres de la methode
- sprint 6 ter : on recuprer le /{id} depuis l url et on le mappe avec un parametre de la methode annotee nommme comme le nom de la variable dans l url {id}

- sprint 7 : on cree une annotation httpMethod et dans on remplace le hasshmap par une classe qui contient l url et la methode http (GET, POST, DELETE, PUT) et on fait le mapping en fonction de la methode http utilisee dans la requete

- sprint 8 : en parametre de methode dans le controller on peut mettre un hashmap de String Object qui va recuperer tous les parametres de la requete et ce hashMap sera system.out.print
- sprint 8 bis : on peut recuperer dans les parametres de la methode une classe ou plusieurs via le fait que dans les vues seront mis dans le case des names des input : le nom de la classe + . + nom de l attribut ( ex : user.name, user.age ) et on remplit une instance de la classe avec les parametres recuperees et on l envoie a la methode du controller mais attention a la casse

