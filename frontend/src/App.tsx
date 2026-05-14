import { ThemeToggle } from "./layout/ThemeToggle";

//Wire Everything Together
function App() {
  return (<>
    <div className="flex items-center gap-3">
      <ThemeToggle />
    </div>
  </>);
}

export default App;
