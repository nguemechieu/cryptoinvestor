
import './App.css';
import logo from './logo.svg';
import { Footer,Blog,Header,Possibility,whatGP3} from './containers'
import {Brand, Cta, Feature, Navbar,Articles} from "./components";

function App() {
  return (
      <div className="App">
    <div className="gradient_bg">
      <Navbar />
        <Header/>
         </div><Brand/>
           <whatGP3/>
            <Feature />
             <Possibility />
             <Cta />
               <Blog />
                  <Articles/>
                   <Footer />
      </div>


  );
}

export default App;
