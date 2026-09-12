# Conclusiones

Este taller partió de un proyecto que ya tenía una base sólida (arquitectura limpia, casos de
uso unitarios bien probados, Testcontainers y Pact configurados) y se enfocó en cerrar
exactamente los huecos que el enunciado señalaba explícitamente: los 4 casos de negocio que
faltaban contra una base de datos H2 real, los mismos casos y uno más (JSON malformado) a nivel
HTTP, una tercera interacción de contrato, y el reto de agregar validación de entrada con
`@Valid`.

Ese último paso no fue solo "agregar una anotación": escribir primero la prueba que debía fallar
(antes de anotar nada) expuso dos defectos reales y no simulados —una `NullPointerException` no
manejada y un dato inválido aceptado en silencio— que ninguna de las pruebas anteriores del
proyecto había encontrado, precisamente porque nadie había escrito todavía el caso que las
expone. Es la lección central del taller: la cobertura de pruebas no es solo un número que se
persigue al final, sino la herramienta que hace visibles los defectos que el código ya tenía.

La revisión de cobertura combinada llevó a una segunda lección menos esperada: el `pom.xml`
original generaba dos reportes de JaCoCo separados que, por diseño, nunca podían sumar el 80 %
global exigido por la rúbrica aunque las pruebas subyacentes fueran suficientes — hacía falta
fusionar explícitamente los dos archivos de ejecución. Y de paso, al investigar por qué un
paquete tenía cobertura baja, apareció un constructor de `PersonDTO` que ningún código de
producción ni de prueba usaba: código muerto que una prueba jamás iba a cubrir porque nada lo
llamaba, y que la limpieza (no una prueba nueva) fue lo que correspondía eliminar.

Ver también: [[Reflexion]], [[Resultados-cobertura]], [[Defectos-y-Diseno]].
