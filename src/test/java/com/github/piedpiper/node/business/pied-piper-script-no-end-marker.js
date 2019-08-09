const args = process.argv.slice(2)
const input = JSON.parse(args[0])

%s

output = execute(input)

console.log("== Begin ResultAtom8NodeJsExecution ==")
console.log(JSON.stringify(output))
